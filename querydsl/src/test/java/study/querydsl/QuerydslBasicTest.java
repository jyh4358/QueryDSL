package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);


        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    @DisplayName("JPQL 테스트")
    public void startJPQL() {

        // member1 찾기
        Member result = em.createQuery("select m from Member m where m.username=:username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        Assertions.assertThat(result.getUsername()).isEqualTo("member1");

    }

    @Test
    @DisplayName("Querydsl 테스트")
    public void startQuerydsl() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em); // EntityManeger를 넣어줘야한다.
        QMember m = new QMember("m"); // querydsl에 사용하기 위한 Q 타입 생성, 파라미터로 별칭
        //QMember.member 를 static import를 하여 사용하는 것을 권장
        Member result = queryFactory
                .selectFrom(m)
                .where(m.username.eq("member1"))    // 자동으로 파라미터 바인딩해준다.
                .fetchOne();
        // JPQL은 오타 발생시 런타임 오류가 발생하지만 Querydsl은 컴파일 오류로 실시간으로 알 수 있다.
        Assertions.assertThat(result.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("검색 조건 쿼리")
    public void search() {
        Member result = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .offset(1)
                .groupBy()
                .fetchOne();


        Assertions.assertThat(result.getUsername()).isEqualTo("member1");

    }
}
