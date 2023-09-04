package com.joy.entity;

import lombok.*;

/**
 * @author Joy
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ChatMsg {

    private String userId;

    private String groupId;

    private String ip;
}