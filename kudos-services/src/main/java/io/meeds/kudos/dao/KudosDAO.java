/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.kudos.dao;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.meeds.kudos.entity.KudosEntity;
import io.meeds.kudos.entity.KudosReceiverResult;

public interface KudosDAO extends JpaRepository<KudosEntity, Long> {

  List<KudosEntity> findByCreatedDateBetweenOrderByCreatedDateDesc(long startDateInSeconds,
                                                                   long endDateInSeconds,
                                                                   Limit limit);

  List<KudosEntity> findByCreatedDateBetweenAndEntityTypeOrderByCreatedDateDesc(long startDateInSeconds,
                                                                                long endDateInSeconds,
                                                                                int entityType,
                                                                                Limit limit);

  List<KudosEntity> findByEntityTypeAndEntityIdOrderByCreatedDateDesc(int entityType, long entityId, Limit limit);

  List<KudosEntity> findByCreatedDateBetweenAndReceiverIdAndIsReceiverUserOrderByCreatedDateDesc(long startDateInSeconds,
                                                                                                 long endDateInSeconds,
                                                                                                 long receiverId,
                                                                                                 boolean isReceiverUser,
                                                                                                 Limit limit);

  List<KudosEntity> findByCreatedDateBetweenAndSenderIdOrderByCreatedDateDesc(long startDateInSeconds,
                                                                              long endDateInSeconds,
                                                                              long senderId,
                                                                              Limit limit);

  KudosEntity findByActivityId(Long activityId);

  @Query("""
          SELECT k FROM Kudos k
          WHERE
            k.activityId = ?1
              OR
            (k.entityType in (?2) AND (k.parentEntityId = ?1 OR k.entityId in ?1))
      """)
  List<KudosEntity> findKudosListOfActivity(Long activityId, List<Integer> entityTypes);

  long countByEntityTypeAndEntityId(int entityType, long entityId);

  long countByEntityTypeAndEntityIdAndSenderId(int entityType, long entityId, long senderId);

  long countByCreatedDateBetweenAndReceiverIdAndIsReceiverUser(long startDateInSeconds,
                                                               long endDateInSeconds,
                                                               long receiverId,
                                                               boolean isReceiverUser);

  @Query("""
          SELECT COUNT(k.id) FROM Kudos k
          WHERE
            k.activityId = ?1
              OR
            (k.entityType in (?2) AND (k.parentEntityId = ?1 OR k.entityId in ?1))
      """)
  long countKudosListOfActivity(Long activityId, List<Integer> entityTypes);

  long countByCreatedDateBetweenAndSenderId(long startDateInSeconds, long endDateInSeconds, long senderId);

  @Query("""
         SELECT new io.meeds.kudos.entity.KudosReceiverResult(k.receiverId, COUNT(k)) from Kudos k
         WHERE k.createdDate >= ?1
         AND k.createdDate < ?2
         AND k.receiverId IN ?3
         GROUP BY k.receiverId
      """)
  List<KudosReceiverResult> countByCreatedDateBetweenAndReceiverIdIn(long startDateInSeconds,
                                                                     long endDateInSeconds,
                                                                     List<Long> receiversId);

}
