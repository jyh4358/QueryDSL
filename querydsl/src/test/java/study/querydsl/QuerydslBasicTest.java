package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    @PersistenceUnit
    EntityManagerFactory emf;

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
    @DisplayName("JPQL ?????????")
    public void startJPQL() {

        // member1 ??????
        Member result = em.createQuery("select m from Member m where m.username=:username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(result.getUsername()).isEqualTo("member1");

    }

    @Test
    @DisplayName("Querydsl ?????????")
    public void startQuerydsl() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em); // EntityManeger??? ??????????????????.
        QMember m = new QMember("m"); // querydsl??? ???????????? ?????? Q ?????? ??????, ??????????????? ??????
        //QMember.member ??? static import??? ?????? ???????????? ?????? ??????
        Member result = queryFactory
                .selectFrom(m)
                .where(m.username.eq("member1"))    // ???????????? ???????????? ??????????????????.
                .fetchOne();
        // JPQL??? ?????? ????????? ????????? ????????? ??????????????? Querydsl??? ????????? ????????? ??????????????? ??? ??? ??????.
        assertThat(result.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    public void search() {
        Member result = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .offset(1)
                .groupBy()
                .fetchOne();


        assertThat(result.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("?????? AND ?????? ?????????")
    public void searchAndParam() {

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"),
                        member.age.eq(10))
                .fetch();

        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    @DisplayName("?????? ?????? ?????????")
    public void resultTest() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .fetch();
        System.out.println("result = " + result);
    }

    @Test
    @DisplayName("?????? ?????????")
    public void sortTest() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }
    
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(0)
                .limit(2)
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);
        }
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("????????? count ??????")
    public void paging2() {
        Long result = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();
        System.out.println("result = " + result);
    }


    @Test
    @DisplayName("?????? ??????")
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
            assertThat(tuple.get(member.count())).isEqualTo(4);
            assertThat(tuple.get(member.age.sum())).isEqualTo(100);
            assertThat(tuple.get(member.age.avg())).isEqualTo(25);
            assertThat(tuple.get(member.age.max())).isEqualTo(40);
            assertThat(tuple.get(member.age.min())).isEqualTo(10);
        }
    }

    @Test
    @DisplayName("?????????/??????")
    public void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    @Test
    @DisplayName("?????? ??????")
    public void join() throws Exception {

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result).extracting("username").containsExactly("member1", "member2");
    }

    @Test
    @DisplayName("?????? ??????")
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        for (Member m : result) {
            System.out.println("m = " + m);
        }

        assertThat(result).extracting("username").containsExactly("teamA", "teamB");
    }

    @Test
    @DisplayName("?????? on??? ??????")
    public void join_on_filtering() {

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }


    @Test
    public void join_where_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void join_on_no_relation() throws Exception {

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }

    @Test
    @DisplayName("?????? ?????? ?????????")
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("?????? ?????? ?????????").isFalse();
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member result = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(result.getTeam());
        assertThat(loaded).as("?????? ?????? ??????").isTrue();

        System.out.println("result = " + result);
    }

    @Test
    @DisplayName("?????? ?????? ??????")
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Member> results = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        for (Member result : results) {
            System.out.println("result = " + result);
        }

    }

    @Test
    @DisplayName("?????? ?????? in ??????")
    public void subQueryIn() {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(20, 30, 40);
    }

    @Test
    @DisplayName("?????? ?????? select ????????? ??????")
    public void subQuerySelect() {
        QMember memberSub = new QMember("memberSub");
        List<Tuple> fetch = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }
    }



    @Test
    @DisplayName("case ?????????")
    public void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("??????")
                        .when(20).then("?????????")
                        .otherwise("??????"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    @DisplayName("case ????????? 2")
    public void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20???")
                        .when(member.age.between(21, 30)).then("21~30???")
                        .otherwise("??????"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    @DisplayName("orderBy?????? case ??? ?????? ??????")
    public void orderByWithCase() {

        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(member.age.between(0, 20)).then(2)
                .when(member.age.between(21, 30)).then(1)
                .otherwise(3);

        List<Tuple> result = queryFactory
                .select(member.username, member.age, rankPath)
                .from(member)
                .orderBy(rankPath.desc())
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }
    }

    @Test
    @DisplayName("?????? ?????????")
    public void constantPlus() {
        Tuple result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetchFirst();

        System.out.println("result = " + result);
    }    
    @Test
    @DisplayName("?????? ?????????")
    public void concatPlus() {
        String result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();


        System.out.println("result = " + result);
    }

    @Test
    @DisplayName("DTO ?????? - ???????????? ??????")
    public void dtoProperty() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("DTO ?????? - ?????? ??????")
    public void dtoField() {
        List<MemberDto> results = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : results) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    @DisplayName("DTO ?????? - ????????? ??????")
    public void dtoConstruct() {
        List<Member> results = queryFactory
                .select(Projections.constructor(Member.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (Member result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test
    @DisplayName("QueryProjection")
    public void queryProjection() {

        List<MemberDto> results = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test
    @DisplayName("BooleanBuilder??? ????????? ????????????")
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

//        ????????? ????????? ?????? ????????? ???????????? ?????? null?????? ?????? ????????? ??????????????? ???
//        ?????? ?????? ?????? BooleanBuilder??? ??????????????? ???????????? ??????.
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    @DisplayName("?????? ??????")
    public void bulkUpdate() {
        long count = queryFactory
                .update(member)
                .set(member.username, "?????????")
                .where(member.age.lt(28))
                .execute();

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("function ??????")
    public void sqlFunction() {
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }




}
