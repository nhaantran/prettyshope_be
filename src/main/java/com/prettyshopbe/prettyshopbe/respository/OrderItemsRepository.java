package com.prettyshopbe.prettyshopbe.respository;

import com.prettyshopbe.prettyshopbe.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<OrderItem,Integer> {
}
