package jpabook.jpashop.repository.query;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.dto.query.GetOrderFlatQueryDto;
import jpabook.jpashop.dto.query.GetOrderQueryDto;
import jpabook.jpashop.dto.query.GetOrdersQueryDto;
import jpabook.jpashop.dto.query.OrderItemQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    public List<GetOrderQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.dto.query.GetOrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", GetOrderQueryDto.class)
                .getResultList();
    }

    public List<GetOrdersQueryDto> findOrdersQueryDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.dto.query.GetOrdersQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", GetOrdersQueryDto.class)
                .getResultList();
    }

    public List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.dto.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderItemQueryDto> findOrderItemsByOptimization(List<Long> orderIds) {
        return em.createQuery(
                "select new jpabook.jpashop.dto.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
    }

    public List<GetOrderFlatQueryDto> findOrderItemsByFlatOpt() {
        return em.createQuery(
                "select new jpabook.jpashop.dto.query.GetOrderFlatQueryDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", GetOrderFlatQueryDto.class)
                .getResultList();
    }

    //    public List<GetOrdersQueryDto> findOrdersQueryDtosAddedOrderItems() {
//        List<GetOrdersQueryDto> result = findOrdersQueryDtos();
//        result.forEach(o -> {
//            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
//            o.setOrderItems(orderItems);
//        });
//        return result;
//    }
}
