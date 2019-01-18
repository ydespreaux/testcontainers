package com.github.ydespreaux.testcontainers.kafka.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class WorkerInfo implements Serializable {

    private String version;
    private String commit;
}
