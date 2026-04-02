package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.example.expert.domain.todo.entity.QTodo;

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
}