package com.joy.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * @author Joy
 */
@Slf4j
@Component
public class DefaultWaitQueueExecutor extends AbstractWaitQueueExecutor {

    private BiFunction<String, String, Boolean> consumer;

    public DefaultWaitQueueExecutor(ThreadPoolTaskExecutor taskExecutor) {
        super(taskExecutor);
    }

    @Override
    public boolean processByQueueName(String queueName, String subject, Object extData) {
        log.debug("队列运行程序 queueName:{} subject:{}", queueName, subject);
        if (null != getConsumer()) {
            return getConsumer().apply(queueName, subject);
        }
        return false;
    }

    public BiFunction<String, String, Boolean> getConsumer() {
        return consumer;
    }

    public void setConsumer(BiFunction<String, String, Boolean> consumer) {
        this.consumer = consumer;
    }
}