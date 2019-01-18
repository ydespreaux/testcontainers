package com.github.ydespreaux.testcontainers.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopicConfiguration {

    /**
     * Topic's name
     */
    private String name;
    /**
     * Number of partitions
     */
    private int partitions = 3;
    /**
     * topics's policy
     */
    private boolean compact = false;

}
