package com.peykasa.audit.domain.model;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class Order {
    private Sort.Direction direction;
    private String property;

    public Order() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Order order = (Order) o;

        return property != null && order.property != null && property.equals(order.property);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (property != null ? property.hashCode() : 0);
        return result;
    }
}
