package com.springpageable.service;

import com.springpageable.dto.FutureDeviceDTO;
import com.springpageable.dto.GetFutureDeviceResponseDTO;
import com.springpageable.exception.ConflictException;
import com.springpageable.mapper.FutureDeviceMapper;
import com.springpageable.repository.FutureDeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@Slf4j
public class FutureDeviceService {

  private final FutureDeviceRepository futureDeviceRepository;

  private final FutureDeviceMapper futureDeviceMapper;

  @Autowired
  public FutureDeviceService(
      FutureDeviceRepository futureDeviceRepository, FutureDeviceMapper futureDeviceMapper) {
    this.futureDeviceRepository = futureDeviceRepository;
    this.futureDeviceMapper = futureDeviceMapper;
  }

  /**
   * Find all future devices
   *
   * @param p - pagination object containing page size, page number and sort parameters
   * @param searchParameter - term to search by serialNumber, productId or customerName
   * @return Page of {@link GetFutureDeviceResponseDTO}
   */
  public Page<GetFutureDeviceResponseDTO> retrieveFutureDevices(
      Pageable p, String searchParameter) {
    var futureDevices = futureDeviceRepository.findAll();
    var devices =
        futureDevices.stream()
            .map(futureDeviceMapper::futureDeviceToFutureDeviceResponseDTO)
            .collect(Collectors.toList());
    return new PageImpl<>(devices, p, devices.size());
  }

  /**
   * Creates new record in device future table, containing combination between
   * serialNumber,productId and customerId
   *
   * @param futureDeviceDTO - {@link FutureDeviceDTO} object containing the new data
   */
  public void createFutureDevice(FutureDeviceDTO futureDeviceDTO) {

    var futureDevice = futureDeviceMapper.futureDeviceDTOToFutureDevice(futureDeviceDTO);
    try {
      futureDeviceRepository.save(futureDevice);
    } catch (DataIntegrityViolationException e) {
      String errMsg =
          String.format(
              "Combination with serial number %s,productId %s and customerId %d already exists",
              futureDeviceDTO.getSerialNumber(),
              futureDeviceDTO.getProductId(),
              futureDeviceDTO.getCustomerId());
      log.error(errMsg);
      throw new ConflictException(errMsg);
    }
  }
}