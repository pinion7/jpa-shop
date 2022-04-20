package jpabook.jpashop.service.query;

import org.springframework.transaction.annotation.Transactional;

/** OSIV에 대한 고찰
 * 1. OSIV: true의 문제점 - DB커넥션 풀이 말라버리는 문제가 발생한다. 트랙픽이 많은 서비스일수록 true는 불가능하다.
 *
 * 2. OSIV: false인 상황에서의 유지보수
 * OrderService: 핵심 비즈니스 로직을 위치시킨다.
 * OrderQueryService: 화면이나 API에 맞춘 서비스를 위치시킨다. (주로 읽기 전용 트랜잭션 사용)
 * 보통 서비스 계층에서 트랜잭션을 유지하는 걸 추천한다. 두 서비스 모두 트랜잭션을 유지하면서 지연 로딩을 사용할 수 있다.
 * 작은 규모의 어플리케이션이면 OrderService안에 전부 때려박아도 괜찮다.
 * 허나 규모가 커질수록 조회관련 코드의 복잡성이 높아지기 때문에 OrderQueryService를 추가해서 관리해주는 것이 좋은 것이다.
 * RDBMS에서 대부분의 DB 성능이슈는 CUD가아닌 R에서 발생한다. 그만큼 SQL 조회는 한없이 복잡한 쿼리를 날려야할 수 있기 때문이다.
 *
 * 참고1: 고객 서비스의 실시간 API는 OSIV를 끄고, ADMIN 처럼 커넥션을 많이 사용하지 않는 곳에서는 OSIV를 켜는 것이 추천된다.
 * 참고2: OSIV에 관해 더 깊이 알고 싶으면 자바 ORM 표준 JPA 프로그래밍 13장 웹 애플리케이션과 영속성 관리를 참고하자.
 */
// 이런식으로 별도의 OrderQueryService 만들어서, 트랜잭션 내에서 지연로딩이 끝나게끔 모든 db관련 로직을 넣어두고
// 컨트롤러에서는 이걸 호출하는 방식으로 코드를 짜면 OSIV: false인 상황에 오류가 터지는 걸 해결할 수 있음
@Transactional(readOnly = true)
public class OrderQueryService {
}
