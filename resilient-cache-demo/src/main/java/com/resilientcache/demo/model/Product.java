package com.resilientcache.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "products")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "category")
    private String category;

    @Column(name = "discounted_price")
    private String discountedPrice;

    @Column(name = "actual_price")
    private String actualPrice;

    @Column(name = "rating")
    private String rating;

    @Column(name = "about_product")
    private String aboutProduct;

    public Product() {
    }

    public Product(String productId, String productName, String category, String discountedPrice, String actualPrice, String rating, String aboutProduct) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.discountedPrice = discountedPrice;
        this.actualPrice = actualPrice;
        this.rating = rating;
        this.aboutProduct = aboutProduct;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(String discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public String getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(String actualPrice) {
        this.actualPrice = actualPrice;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getAboutProduct() {
        return aboutProduct;
    }

    public void setAboutProduct(String aboutProduct) {
        this.aboutProduct = aboutProduct;
    }

    @Override
    public String toString() {
        return "Product[ID=" + productId + ", Name=" + productName + ", Price=" + discountedPrice + "]";
    }
}

