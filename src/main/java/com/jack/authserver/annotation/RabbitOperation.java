package com.jack.authserver.annotation;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RabbitOperation<T> {

    private OP op;

    private T data;

    public enum OP{
        ADD, DELETE, UPDATE, SEARCH
    }
}
