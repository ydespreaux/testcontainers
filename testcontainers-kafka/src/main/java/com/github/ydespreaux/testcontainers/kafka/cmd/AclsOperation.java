package fr.laposte.an.testcontainers.kafka.acls;

public enum OperationAcls {
    ALL("All"), READ("Read"), WRITE("Write"), DESCRIBE("Describe");

    private final String operationName;

    OperationAcls(String operationName) {
        this.operationName = operationName;
    }

    public String operationName(){
        return this.operationName;
    }
}
