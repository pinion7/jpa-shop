package jpabook.jpashop.service;

import jpabook.jpashop.dto.UpdateItemDto;
import jpabook.jpashop.form.BookForm;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public void updateItem(Long itemId, UpdateItemDto updateItemDto) {
        Book findItem = (Book) itemRepository.findOne(itemId);
        findItem.setName(updateItemDto.getName());
        findItem.setPrice(updateItemDto.getPrice());
        findItem.setStockQuantity(updateItemDto.getStockQuantity());
//        findItem.setAuthor(updateItemDto.getAuthor());
//        findItem.setIsbn(updateItemDto.getIsbn());
    }

    public Item findItemOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }
}
