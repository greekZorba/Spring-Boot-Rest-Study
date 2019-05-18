package com.community.rest.repository;

import com.community.rest.domain.Board;
import com.community.rest.domain.projection.BoardOnlyContainTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

// 기본 path가 boards임. path에 입력해서 원하는대로 경로 설정 가능
@RepositoryRestResource(excerptProjection = BoardOnlyContainTitle.class, path = "boards")
// 아래 'exported = false'은 특정 리포지토리, 쿼리 메서드, 필드를 노출하고 싶지 않을 때 사용한다.
//@RepositoryRestResource(exported = false)
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    <S extends Board> S save(S entity);

    @RestResource(path = "query")
//    @RestResource(exported = false)
    List<Board> findByTitle(@Param("title") String title);
}
