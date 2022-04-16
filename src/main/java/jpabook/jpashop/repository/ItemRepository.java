package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        // jpa로 저장하기 전까진 id값이 없기 때문에 em.persist로 신규로 등록하면서 id값도 부여
        if (item.getId() == null) {
            em.persist(item);
        } else {
            // 그렇지 않으면 merge를 사용. 업데이트와 비슷하다고 보면 됨.
            // 근데 준영속상태를 전부 업데이트 적용하는 건데, 문제는 선택적 적용이 불가해서 값이 없는 건 null로 업데이트해서 위험 (그래서 실무에선 활용 안하게됨)
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList(); // findAll은 jpql로 수행해야함! 뜨헉
    }
}
