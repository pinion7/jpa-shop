package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager em;

    @Test
    void updateTest() {
        // given
        Book book = em.find(Book.class, 1L);

        // when
        book.setName("asdfsafd"); // 알아서 변경감지(dirty checking)해서 db에 업데이트 해줌 vs but 준영속 엔티티는 set해도 변경감지 안됨 (em.merge 필요)

        //then
    }

}
