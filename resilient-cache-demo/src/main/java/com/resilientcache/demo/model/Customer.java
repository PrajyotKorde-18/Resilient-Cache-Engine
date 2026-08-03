package com.resilientcache.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "customers")
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "customer_index")
    private Long customerIndex;

    @Column(name = "customer_names")
    private String customerName;

    public Customer() {
    }

    public Customer(Long customerIndex, String customerName) {
        this.customerIndex = customerIndex;
        this.customerName = customerName;
    }

    public Long getCustomerIndex() {
        return customerIndex;
    }

    public void setCustomerIndex(Long customerIndex) {
        this.customerIndex = customerIndex;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String toString() {
        return "Customer[Index=" + customerIndex + ", Name=" + customerName + "]";
    }
}
