package study.querydsl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.entity.Member;

import java.util.List;
// MemberQueryRepository로 나눠서 사용해도 된다.
// Entity를 검색하는 것과 같은 핵심 비즈니스 로직은 MemberRepository에 넣고
// 한 곳에서만 쓰는 특정 비즈니스 로직은 따로 MemberQeuryRepository로 나눠서 작성하는 것이 유지보수 관점에서 좋다.
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByUsername(String username);
}
