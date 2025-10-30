package com.example.verificationservice.verify_request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VerifyRequestRepository extends JpaRepository<VerifyRequest, String>, JpaSpecificationExecutor<VerifyRequest> {
}
