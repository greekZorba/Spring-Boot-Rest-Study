package com.web.resolver;

import com.web.annotation.SocialUser;
import com.web.domain.User;
import com.web.domain.enums.SocialType;
import com.web.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    private UserRepository userRepository;

    public UserArgumentResolver(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(SocialUser.class) != null &&
                parameter.getParameterType().equals(User.class);
    }

    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpSession session = ((ServletRequestAttributes)RequestContextHolder
                .currentRequestAttributes()).getRequest().getSession();
        User user = (User) session.getAttribute("user");
        return getUser(user, session);
    }

    private User getUser(User user, HttpSession session){
        if(user == null){
            try{
                OAuth2Authentication authentication = (OAuth2Authentication)
                        SecurityContextHolder.getContext().getAuthentication();
                Map<String, String> map = (HashMap<String, String>)
                        authentication.getUserAuthentication().getDetails();
                User convertUser = convertUser(String.valueOf(authentication
                                                                .getAuthorities()
                                                                .toArray()[0]), map);
                user = userRepository.findByEmail(convertUser.getEmail());

                if(user == null){
                    user = userRepository.save(convertUser);
                }

                setRoleIfNotSame(user, authentication, map);
                session.setAttribute("user", user);

            }catch (ClassCastException e){
                return user;
            }
        }

        return user;
    }

    private User convertUser(String authority, Map<String, String> map){
        if(SocialType.FACEBOOK.isEquals(authority)){
            return getModernUser(SocialType.FACEBOOK, map);
        }
        else if(SocialType.GOOGLE.isEquals(authority)){
            return getModernUser(SocialType.GOOGLE, map);
        }
        else if(SocialType.KAKAO.isEquals(authority)){
            return getModernUser(SocialType.KAKAO, map);
        }
        else{
            return null;
        }
    }

    private User getModernUser(SocialType socialType, Map<String, String> map){
        return User.builder()
                .name(map.get("name"))
                .email(map.get("email"))
                .principal(map.get("id"))
                .socialType(socialType)
                .createdDate(LocalDateTime.now())
                .build();
    }
}
