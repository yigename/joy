package com.joy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Joy
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaitQueueInfo {

    private Long rank;

    private Long fromTime;
}
