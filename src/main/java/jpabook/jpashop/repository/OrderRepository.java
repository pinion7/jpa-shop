package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * 1. QueryDSL 방식 - 실무에서는 동적쿼리로 이걸 써야함. (추후 추가 예정)
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        return em.createQuery("select o from Order o join o.member m" +
                        " where o.status = :status" +
                        " and m.name like :name", Order.class)
                .setParameter("status", orderSearch.getOrderStatus())
                .setParameter("name", orderSearch.getMemberName())
//                .setFirstResult(100) // 100번째부터 가져오겠다 (페이징 방법)
                .setMaxResults(1000) // 데이터를 최대 1000개까지만 가져오겠다 (위에꺼랑 연동하면 100번째부터 시작해서 1000개 가져오겠다는 것)
                .getResultList();

//        QOrder order = QOrder.order;
//        QMember member = QMember.member;
//
//        return query
//                .select(order)
//                .from(order)
//                .join(order.member, member)
//                .where(statusEq(orderSearch.getOrderStatus()),
//                        nameLike(orderSearch.getMemberName()))
//                .limit(1000)
//                .fetch();
    }

//    private BooleanExpression statusEq(OrderStatus statusCond) {
//        if (statusCond == null) {
//            return null;
//        }
//        return order.status.eq(statusCond);
//    }
//
//    private BooleanExpression nameLike(String nameCond) {
//        if (!StringUtils.hasText(nameCond)) {
//            return null;
//        }
//    }


    /**
     * 2. JPA ciriteria 방식 (아래의 3번보다는 낫지만 절대 추천 하고싶진 않음...)
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    /**
     * 3. JPQL string 합치기 방식 (더이상 실무에서 사용하지 않는 사장된 방식...)
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    // fetch 조인!
    // order를 조회하는데, order를 기준으로 member랑 delivery를 다같이 조인한 다음에 아예 select 절에 이걸 다 넣고 한번에 다 땡겨오는 개념!
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    // 일대다 컬렉션 데이터를 페치 조인하여 조회 하는 케이스!
    // 중요한 키워드는 distinct!
    // jpa에서 이 키워드를 넣으면 두가지 기능을 해줌
    // 1. db에 distinct가 붙은 쿼리를 날려줌 (하지만 이것만으로는 fetch 조인 되었을 때 완전히 동일한 레코드가 아니면 중복으로 인정을 못해줌)
    // 2. from 뒤에 오는 엔티티가 중복인 경우에, 그 중복을 걸러서 하나만 컬렉션에 담아줌! (이 2번째 기능 덕에 실제 중복되지 않은 데이터로 필터가 성공!)
    // -> 하지만 치명적인 단점: 페이징이 불가능!!!!! 앙대~~~~~~~~~~~~~~~~~~
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList();

    }

    // 일단 일대다를 빼고 페치 조인 및 페이징 적용!
    // service로직 돌아가서 루프 돌면서 일대다 컬렉션에 해당하는 데이터도 초기화로 끌어오되, yml 파일에 in query 사이즈를 크게 줘서 쿼리를 최소화!
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
