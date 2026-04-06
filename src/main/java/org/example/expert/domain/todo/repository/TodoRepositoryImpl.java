package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.response.QTodoSearchResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.example.expert.domain.todo.entity.QTodo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
//  Spring Data JPA가 Repository이름 + Impl 규칙으로 자동 인식 -> TodoRepositoryImpl로 생성
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory; // QueryDSL 쿼리 실행 도구

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        Todo todo = queryFactory
                .selectFrom(QTodo.todo) // SELECT + FROM todos
                .leftJoin(QTodo.todo.user, QUser.user).fetchJoin()  // LEFT JOIN FETCH users
                .where(QTodo.todo.id.eq(todoId))    // WHERE id + todoId
                .fetchOne();    // 결과는 1개만

        return Optional.ofNullable(todo);   // null이면 Optional.empty() 반환
    }

    @Override
    public Page<TodoSearchResponse> searchTodos(
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String nickname,
            Pageable pageable
    ) {
        QTodo todo = QTodo.todo;
        QUser user = QUser.user;
        QManager manager = QManager.manager;
        QComment comment = QComment.comment;

        List<TodoSearchResponse> results = queryFactory
                .select(new QTodoSearchResponse(
                        todo.title,                    // 제목
                        manager.count(),               // 담당자 수
                        comment.count()                // 댓글 수
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(
                        titleContains(title),          // 제목 부분 검색
                        createdAtBetween(startDate, endDate), // 생성일 범위
                        nicknameContains(nickname)     // 닉네임 부분 검색
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())        // 생성일 최신순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(
                        titleContains(title),
                        createdAtBetween(startDate, endDate),
                        nicknameContains(nickname)
                )
                .groupBy(todo.id)
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }

    // 제목 부분 검색 조건 (null이면 무시)
    private BooleanExpression titleContains(String title) {
        return title != null ? QTodo.todo.title.containsIgnoreCase(title) : null;
    }

    // 생성일 범위 검색 조건 (null이면 무시)
    private BooleanExpression createdAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return QTodo.todo.createdAt.between(startDate, endDate);
        } else if (startDate != null) {
            return QTodo.todo.createdAt.goe(startDate);
        } else if (endDate != null) {
            return QTodo.todo.createdAt.loe(endDate);
        }
        return null;
    }

    // 닉네임 부분 검색 조건 (null이면 무시)
    private BooleanExpression nicknameContains(String nickname) {
        return nickname != null ? QUser.user.nickname.containsIgnoreCase(nickname) : null;
    }
}