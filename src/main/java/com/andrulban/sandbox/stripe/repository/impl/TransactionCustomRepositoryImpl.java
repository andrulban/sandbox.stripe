package com.andrulban.sandbox.stripe.repository.impl;

import com.andrulban.sandbox.stripe.entity.Transaction;
import com.andrulban.sandbox.stripe.entity.Transaction_;
import com.andrulban.sandbox.stripe.repository.TransactionCustomRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TransactionCustomRepositoryImpl implements TransactionCustomRepository {

  @PersistenceContext private EntityManager entityManager;

  @Override
  public List<Transaction> getTransactionsByFilter(
      Map<String, Object> filters,
      String field,
      boolean isAscending,
      Integer firstResult,
      Integer maxResults) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
    Root<Transaction> root = cq.from(Transaction.class);

    SingularAttribute sortField = null;
    if (field != null) {
      if (field.equals(Transaction_.id.getName())) {
        sortField = Transaction_.id;
      } else if (field.equals(Transaction_.description.getName())) {
        sortField = Transaction_.description;
      } else if (field.equals(Transaction_.amount.getName())) {
        sortField = Transaction_.amount;
      } else if (field.equals(Transaction_.currency.getName())) {
        sortField = Transaction_.currency;
      } else if (field.equals(Transaction_.status.getName())) {
        sortField = Transaction_.status;
      } else if (field.equals(Transaction_.creationDate.getName())) {
        sortField = Transaction_.creationDate;
      }
    }

    if (sortField != null) {
      if (isAscending) {
        cq.orderBy(cb.asc(root.get(sortField)), cb.asc(root.get(Transaction_.id)));
      } else {
        cq.orderBy(cb.desc(root.get(sortField)), cb.asc(root.get(Transaction_.id)));
      }
    } else {
      cq.orderBy(cb.asc(root.get(Transaction_.id)));
    }

    cq.where(getFilteringConditions(cb, root, filters));

    return entityManager
        .createQuery(cq)
        .setFirstResult(Optional.ofNullable(firstResult).orElse(0))
        .setMaxResults(Optional.ofNullable(maxResults).orElse(20))
        .getResultList();
  }

  @Override
  public Long countTransactionsByFilter(Map<String, Object> filters) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<Transaction> root = cq.from(Transaction.class);

    cq.where(getFilteringConditions(cb, root, filters));
    cq.select(cb.countDistinct(root.get(Transaction_.id)));
    return entityManager.createQuery(cq).getSingleResult();
  }

  private Predicate[] getFilteringConditions(
      CriteriaBuilder cb, Root<Transaction> root, Map<String, Object> filters) {
    List<Predicate> predicates = new ArrayList<>();
    for (Map.Entry<String, Object> entry : filters.entrySet()) {
      if ("description".equals(entry.getKey())) {
        predicates.add(
            cb.like(
                cb.lower(root.get(Transaction_.description)),
                "%" + ((String) entry.getValue()).toLowerCase() + "%"));
      } else if ("amount".equals(entry.getKey())) {
        predicates.add(cb.equal(root.get(Transaction_.amount), entry.getValue()));
      } else if ("amountFrom".equals(entry.getKey())) {
        predicates.add(
            cb.greaterThanOrEqualTo(root.get(Transaction_.amount), (Long) entry.getValue()));
      } else if ("amountTo".equals(entry.getKey())) {
        predicates.add(
            cb.lessThanOrEqualTo(root.get(Transaction_.amount), (Long) entry.getValue()));
      } else if ("userId".equals(entry.getKey())) {
        predicates.add(cb.equal(root.get(Transaction_.user), entry.getValue()));
      }
    }
    return predicates.toArray(new Predicate[] {});
  }
}
