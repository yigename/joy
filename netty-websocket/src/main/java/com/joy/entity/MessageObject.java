package com.joy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @author Joy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageObject implements Serializable {


    private String code;

    private Object data;
}