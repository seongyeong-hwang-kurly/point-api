package com.kurly.cloud.point.api.point.repository.impl;

import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.repository.MemberPointHistoryCustomRepository;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class MemberPointHistoryCustomRepositoryImpl implements MemberPointHistoryCustomRepository {

  @PersistenceContext
  private EntityManager em;

  @Override
  public Page<MemberPointHistory> getMemberPointHistories(MemberPointHistoryListRequest request) {
    return new PageImpl<>(getList(request),
        PageRequest.of(request.getPage(), request.getSize()),
        getCount(request));
  }

  private List<MemberPointHistory> getList(MemberPointHistoryListRequest request) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<MemberPointHistory> query = cb.createQuery(MemberPointHistory.class);
    Root<MemberPointHistory> from = query.from(MemberPointHistory.class);
    query.where(getPredicate(request, cb, from));
    query.orderBy(cb.desc(from.get("regTime")), cb.desc(from.get("seq")));
    return em.createQuery(query.select(from))
        .setMaxResults(request.getSize())
        .setFirstResult(request.getPage() * request.getSize())
        .getResultList();
  }

  private Long getCount(MemberPointHistoryListRequest request) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);
    Root<MemberPointHistory> from = query.from(MemberPointHistory.class);
    query.where(getPredicate(request, cb, from));
    return em.createQuery(query.select(cb.count(from))).getSingleResult();
  }

  private Predicate getPredicate(MemberPointHistoryListRequest request, CriteriaBuilder cb,
                                 Root<MemberPointHistory> from) {
    List<Predicate> predicateList = new ArrayList<>();
    predicateList.add(cb.equal(from.get("memberNumber"), request.getMemberNumber()));
    if (!request.isIncludeHidden()) {
      predicateList.add(cb.equal(from.get("hidden"), false));
    }
    if (Objects.nonNull(request.getRegDateTimeFrom())) {
      predicateList.add(cb.greaterThanOrEqualTo(from.get("regTime"),
          DateTimeUtil.toLocalDateTime(request.getRegDateTimeFrom())));
    }
    if (Objects.nonNull(request.getRegDateTimeTo())) {
      predicateList.add(cb.lessThanOrEqualTo(from.get("regTime"),
          DateTimeUtil.toLocalDateTime(request.getRegDateTimeTo())));
    }
    return cb.and(predicateList.toArray(new Predicate[] {}));
  }

}
