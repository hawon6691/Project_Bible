package com.pbshop.java.spring.maven.jpa.postgresql.pcbuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;

@Entity
@Table(name = "pc_build_parts")
public class PcBuildPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pc_build_id", nullable = false)
    private PcBuild pcBuild;
    @Column(name = "part_type", nullable = false)
    private String partType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @Column(nullable = false)
    private int quantity = 1;
    public Long getId() { return id; }
    public PcBuild getPcBuild() { return pcBuild; }
    public void setPcBuild(PcBuild pcBuild) { this.pcBuild = pcBuild; }
    public String getPartType() { return partType; }
    public void setPartType(String partType) { this.partType = partType; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
