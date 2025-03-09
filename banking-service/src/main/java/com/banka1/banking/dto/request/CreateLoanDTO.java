package com.banka1.banking.dto.request;

import com.banka1.banking.models.helper.InterestType;
import com.banka1.banking.models.helper.LoanType;
import lombok.Data;

@Data
public class CreateLoanDTO {
    private LoanType loanType;
    private Integer numberOfInstallments;
    private InterestType interestType;
    private Double nominalRate;
    private Double effectiveRate;
    private Double loanAmount;
    private Double duration;
    private Long allowedDate;
    private Double monthlyPayment;
    private Long accountId; //ne mozemo bas da trazimo Account type ali mozemo da povezemo sa Acc repo i da vadimo odatle
}
