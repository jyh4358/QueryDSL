# QueryDSL

김영한님의 인프런 [실전!QueryDSL](https://www.inflearn.com/course/Querydsl-%EC%8B%A4%EC%A0%84/dashboard) 강의 공부 공간입니다.  

[개인 블로그](https://jddng.tistory.com/category/Spring/Querydsl)


## 내용정리
22.03.22
  - [QueryDSL 설정 및 검증](https://jddng.tistory.com/331)
  
22.03.23 ~ 22.03.24
  - [QueryDSL 기본 문법](https://jddng.tistory.com/334)
    1. Querydsl 사용 방법
    2. Q-Type
    3. 검색 조건
    4. 결과 조회
    5. 정렬
    6. 페이징
    7. 집합 - 집합 함수, groupby, having
    8. 조인 - on절, 페치 조인
    9. 서브 쿼리
    10. case
    11. 상수, 문자 더하기

22.03.25
- [프로젝션에 따른 결과 반환](https://jddng.tistory.com/336)
- [DTO로 조회하기](https://jddng.tistory.com/337)
- [BooleanBuilder를 이용한 동적 쿼리](https://jddng.tistory.com/338)
- [where절을 이용한 동적 쿼리](https://jddng.tistory.com/339)
- [수정, 삭제 벌크 연산](https://jddng.tistory.com/340)

22.03.26
- 순수 JPA와 Querydsl
  - [Repository에서 Querydsl 사용](https://jddng.tistory.com/341)
  - [BooleanBuilder를 사용한 동적 쿼리와 성능 최적화](https://jddng.tistory.com/342)
  - [where절을 이용한 동적 쿼리와 성능 최적화](https://jddng.tistory.com/343)
- [Spring Data JPA와 Querydsl](https://jddng.tistory.com/344)
- [Spring Data JPA에서 제공하는 페이징 활용](https://jddng.tistory.com/345)
    
    
## 구현 

22.03.22
  - 예제 도메인 생성. (Member, Team)
  - 예제 도메인 테스트

22.03.24
- 기본 문법 테스트. QuerydslBasicTest

22.03.24
- initMember - init 데이터 생성
- MemberController - api controller 구현
- Repository 구현
  - MemberQuerydslRepository - Querydsl 사용
  - MemberRepository - Spring Data JAP에서 제공하는 JpaReposiotry를 상속
  - MemberRepositoryCustom, MemberRepositoryImpl - Custom Repository 구현
- MemberQuerydslRepository, MemberRepository - 구현한 Repository test


  