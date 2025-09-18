package com.syos.infrastructure.persistence.repositories;

import com.syos.application.ports.out.CategoryRepository;
import com.syos.domain.entities.Category;
import com.syos.infrastructure.persistence.entities.CategoryEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of CategoryRepository (minimal functionality).
 */
public class JpaCategoryRepository implements CategoryRepository {
    
    private final EntityManager entityManager;

    public JpaCategoryRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Category save(Category category) {
        if (category == null) throw new IllegalArgumentException("category is null");
        entityManager.getTransaction().begin();
        try {
            CategoryEntity e;
            if (category.getId() == null) {
                e = new CategoryEntity(
                    category.getCategoryCode(), category.getCategoryName(), category.getDescription(), category.getParentCategoryId());
                e.setDisplayOrder(category.getDisplayOrder());
                e.setIsActive(category.isActive());
                entityManager.persist(e);
            } else {
                e = entityManager.find(CategoryEntity.class, category.getId());
                if (e == null) {
                    e = new CategoryEntity(
                        category.getCategoryCode(), category.getCategoryName(), category.getDescription(), category.getParentCategoryId());
                    e.setId(category.getId());
                    e.setDisplayOrder(category.getDisplayOrder());
                    e.setIsActive(category.isActive());
                    entityManager.persist(e);
                } else {
                    e.setCategoryCode(category.getCategoryCode());
                    e.setCategoryName(category.getCategoryName());
                    e.setDescription(category.getDescription());
                    e.setParentCategoryId(category.getParentCategoryId());
                    e.setDisplayOrder(category.getDisplayOrder());
                    e.setIsActive(category.isActive());
                    entityManager.merge(e);
                }
            }
            entityManager.getTransaction().commit();
            return mapToDomain(e);
        } catch (RuntimeException ex) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        CategoryEntity e = entityManager.find(CategoryEntity.class, id);
        return Optional.ofNullable(e).map(this::mapToDomain);
    }

    @Override
    public Optional<Category> findByCategoryCode(String categoryCode) {
        TypedQuery<CategoryEntity> q = entityManager.createQuery(
            "SELECT c FROM CategoryEntity c WHERE UPPER(c.categoryCode) = :code", CategoryEntity.class);
        q.setParameter("code", categoryCode == null ? null : categoryCode.toUpperCase());
        List<CategoryEntity> res = q.getResultList();
        return res.isEmpty() ? Optional.empty() : Optional.of(mapToDomain(res.get(0)));
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        return entityManager.find(CategoryEntity.class, id) != null;
    }

    @Override
    public boolean existsByCategoryCode(String categoryCode) {
        return findByCategoryCode(categoryCode).isPresent();
    }

    @Override
    public List<Category> findAllActive() {
        TypedQuery<CategoryEntity> q = entityManager.createQuery(
            "SELECT c FROM CategoryEntity c WHERE c.isActive = true ORDER BY c.displayOrder, c.categoryName",
            CategoryEntity.class);
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Category> findRootCategories() {
        TypedQuery<CategoryEntity> q = entityManager.createQuery(
            "SELECT c FROM CategoryEntity c WHERE c.parentCategoryId IS NULL AND c.isActive = true ORDER BY c.displayOrder, c.categoryName",
            CategoryEntity.class);
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Category> findByParentCategoryId(Long parentId) {
        TypedQuery<CategoryEntity> q = entityManager.createQuery(
            "SELECT c FROM CategoryEntity c WHERE c.parentCategoryId = :pid AND c.isActive = true ORDER BY c.displayOrder, c.categoryName",
            CategoryEntity.class);
        q.setParameter("pid", parentId);
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<Category> findAll() {
        TypedQuery<CategoryEntity> q = entityManager.createQuery(
            "SELECT c FROM CategoryEntity c ORDER BY c.displayOrder, c.categoryName", CategoryEntity.class);
        return q.getResultList().stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public boolean isActive(Long id) {
        CategoryEntity e = entityManager.find(CategoryEntity.class, id);
        return e != null && Boolean.TRUE.equals(e.getIsActive());
    }

    @Override
    public List<Category> getCategoryHierarchy() {
        // Minimal implementation: return all active categories ordered by display order
        return findAllActive();
    }

    @Override
    public long countActiveCategories() {
        Long count = entityManager.createQuery(
            "SELECT COUNT(c) FROM CategoryEntity c WHERE c.isActive = true", Long.class).getSingleResult();
        return count == null ? 0 : count;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        entityManager.getTransaction().begin();
        try {
            CategoryEntity e = entityManager.find(CategoryEntity.class, id);
            if (e != null) {
                e.setIsActive(false);
                entityManager.merge(e);
            }
            entityManager.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (entityManager.getTransaction().isActive()) entityManager.getTransaction().rollback();
            throw ex;
        }
    }

    private Category mapToDomain(CategoryEntity e) {
        return Category.reconstruct(
            e.getId(),
            e.getParentCategoryId(),
            e.getCategoryCode(),
            e.getCategoryName(),
            e.getDescription(),
            e.getDisplayOrder() == null ? 0 : e.getDisplayOrder(),
            Boolean.TRUE.equals(e.getIsActive()),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }
}
