package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

//  TodoRepositoryCustom 추가 상속을 통해 CRUD + QueryDSL 쿼스텀 쿼리 둘 다 사용 가능
public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    // Todo를 t로 조회
    // LEFT JOIN FETCH: Todo와 연관된 User를 한 번에 같이 가져오기 (N+1 방지)
    // u: user의 별칭 (있어도 없어도 무관)
    // +: 자바에서 문자열이 너무 길면 줄바꿈을 위해 사용됨
    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user " +

            // weather 파라미터가 null이면 조건
            // weather 파라미터가 있으면 해당 날씨만 조회
            "WHERE (:weather IS NULL OR t.weather = :weather) " +

            // weather 와 동일하게 startDate가 null이면 무시 있으면 그 날짜 이후로 수정된 것만 조회
            "AND (:startDate IS NULL OR t.modifiedAt >= :startDate) " +

            // endDate가 null이면 무시, 있으면 그 날짜 이전으로 수정된 것만 조회
            "AND (:endDate IS NULL OR t.modifiedAt <= :endDate) " +

            // 수정일 기준 최신순 정렬
            "ORDER BY t.modifiedAt DESC")

    //  Page<Todo>: 결과를 페이징 처리해서 반환(전체 목록 중 일부만)
    // findAllByOrderByModifiedAtDesc(): 파라미터 Pagable만 받고 조건 없이 무조건 전체 조회만 가능함
    // findByConditions(): 조건을 받아서 null이면 전체 조회, 값이 있으면 필터링 조회를 하기 때문에 메서드 변경
    Page<Todo> findByConditions(
            //  String weather: 자바에서 받는 파라미터
            //  @Param("weather"): 위에 JPQL에서 :weather로 쓴 것과 연결시켜주는 역할
            // 자바의 weather 변수값이 JPQL의 :weather 자리에 들어감
            @Param("weather") String weather,
            // LoclaDateTime: 날짜 + 시간을 담는 자바 타입
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            // 페이징 정보 (몇 페이지, 한 페이지에 몇 개)를 담는 객체
            // @Param 없는 이유: Spring Data JPA가 자동으로 인식하기 때문
            Pageable pageable);

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
