package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional // 클래스 단위 트랜잭션
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage; // 주입

  @Override
  public BinaryContent create(BinaryContentCreateRequest request) {
    String fileName = request.fileName();
    byte[] bytes = request.bytes();
    String contentType = request.contentType();

    // DB에 메타 정보만
    BinaryContent entity = new BinaryContent(
            fileName,
            (long) bytes.length,
            contentType
    );
    entity = binaryContentRepository.save(entity);

    // 실제 byte[] → 스토리지
    binaryContentStorage.put(entity.getId(), bytes);

    return entity;
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public BinaryContent find(UUID binaryContentId) {
    return binaryContentRepository.findById(binaryContentId)
            .orElseThrow(() -> new NoSuchElementException(
                    "BinaryContent with id " + binaryContentId + " not found"));
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public List<BinaryContent> findAllByIdIn(List<UUID> binaryContentIds) {
    return binaryContentRepository.findAllById(binaryContentIds).stream()
            .toList();
  }

  @Override
  public void delete(UUID binaryContentId) {
    if (!binaryContentRepository.existsById(binaryContentId)) {
      throw new NoSuchElementException("BinaryContent with id " + binaryContentId + " not found");
    }
    // 스토리지에서도 지우려면 여기에 로직 추가 (예: binaryContentStorage.delete(binaryContentId))
    binaryContentRepository.deleteById(binaryContentId);
  }
}
