package com.kurly.cloud.point.api.batch.member.entity;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.codec.Hex;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gd_dormant")
public class DormantMember {
  @Transient
  private final String secretKey = "alskadbsqjaduf*@#";
  @Transient
  private Cipher cipher;

  @Id
  @Column(name = "m_no")
  long memberNumber;

  @Column(name = "m_id")
  String memberId;

  @Column(name = "mobile")
  String mobile;

  @Column(name = "address")
  String address;

  @Column(name = "address_sub")
  String addressSub;

  @Column(name = "road_address")
  String roadAddress;

  @Transient
  public String getJibunFullAddress() {
    return decrypt(address) + " " + decrypt(addressSub);
  }

  @Transient
  public String getRoadFullAddress() {
    return decrypt(roadAddress) + " " + decrypt(addressSub);
  }

  @Transient
  public String getMobile() {
    return decrypt(mobile);
  }

  private String decrypt(String encrypted) {
    if (Objects.isNull(cipher)) {
      initCipher();
    }
    try {
      return new String(cipher.doFinal(Hex.decode(encrypted)));
    } catch (IllegalBlockSizeException | BadPaddingException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void initCipher() {
    try {
      this.cipher = Cipher.getInstance("AES");
      this.cipher.init(Cipher.DECRYPT_MODE, generateMySQLAESKey(secretKey));
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
  private SecretKeySpec generateMySQLAESKey(final String key) {
    final byte[] finalKey = new byte[16];
    int i = 0;
    for (byte b : key.getBytes()) {
      finalKey[i++ % 16] ^= b;
    }
    return new SecretKeySpec(finalKey, "AES");
  }

  /**
   * Member 객체로 변환한다.
   */
  public Member toMember() {
    return Member.builder()
        .memberNumber(memberNumber)
        .memberId(memberId)
        .mobile(decrypt(mobile))
        .build();
  }
}
