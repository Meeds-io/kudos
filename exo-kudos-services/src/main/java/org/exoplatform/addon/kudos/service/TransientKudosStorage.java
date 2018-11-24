package org.exoplatform.addon.kudos.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.StringUtils;

import org.exoplatform.addon.kudos.model.Kudos;

/**
 * This is a placeholder class to manage Kudos storage in a transient way
 */
public class TransientKudosStorage implements KudosStorage {

  private Vector<Kudos> kudosVector = new Vector<>();

  private AtomicLong    technicalId = new AtomicLong();

  @Override
  public Kudos getKudoById(long id) {
    return kudosVector.stream().filter(kudos -> kudos.getTechnicalId() == id).findFirst().get();
  }

  @Override
  public void createKudos(Kudos kudos) {
    kudos.setTechnicalId(technicalId.incrementAndGet());
    kudosVector.add(kudos);
  }

  @Override
  public List<Kudos> getAllKudosByMonth(YearMonth yearMonth) {
    return kudosVector.stream()
                      .filter(kudos -> yearMonth.compareTo(YearMonth.from(kudos.getTime())) == 0)
                      .collect(Collectors.toList());
  }

  @Override
  public List<Kudos> getAllKudosByMonthAndEntityType(YearMonth yearMonth, String entityType) {
    return kudosVector.stream()
                      .filter(kudos -> StringUtils.equals(entityType, kudos.getEntityType())
                          && yearMonth.compareTo(YearMonth.from(kudos.getTime())) == 0)
                      .collect(Collectors.toList());
  }

  @Override
  public List<Kudos> getAllKudosByEntity(String entityType, String entityId) {
    return kudosVector.stream()
                      .filter(kudos -> StringUtils.equals(entityType, kudos.getEntityType())
                          && StringUtils.equals(entityId, kudos.getEntityId()))
                      .collect(Collectors.toList());
  }

  @Override
  public List<Kudos> getKudosByMonthAndSender(YearMonth yearMonth, String senderId) {
    return kudosVector.stream()
                      .filter(kudos -> StringUtils.equals(senderId, kudos.getSenderId())
                          && yearMonth.compareTo(YearMonth.from(kudos.getTime())) == 0)
                      .collect(Collectors.toList());
  }

  @Override
  public long countKudosByMonthAndSender(YearMonth yearMonth, String senderId) {
    return kudosVector.stream()
                      .filter(kudos -> StringUtils.equals(senderId, kudos.getSenderId())
                          && yearMonth.compareTo(YearMonth.from(kudos.getTime())) == 0)
                      .count();
  }

  @Override
  public List<Kudos> getKudosByMonthAndReceiver(YearMonth yearMonth, String receiverType, String receiverId) {
    return kudosVector.stream()
                      .filter(kudos -> StringUtils.equals(receiverId, kudos.getReceiverId())
                          && StringUtils.equals(receiverType, kudos.getReceiverType())
                          && yearMonth.compareTo(YearMonth.from(kudos.getTime())) == 0)
                      .collect(Collectors.toList());
  }

}
