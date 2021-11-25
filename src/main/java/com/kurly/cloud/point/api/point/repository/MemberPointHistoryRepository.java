package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberPointHistoryRepository
    extends JpaRepository<MemberPointHistory, Long>, MemberPointHistoryCustomRepository {
    List<MemberPointHistory> findAllByMemberNumber(long memberNumber);
}
