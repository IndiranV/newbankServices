package org.in.com.dto;

import java.util.List;

import org.in.com.dto.enumeration.TransactionTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TermDTO extends BaseDTO<TermDTO> {
	private int sequenceId;
	private List<String> tagList;
	private List<ScheduleDTO> schedule;
	private TransactionTypeEM transactionType;
}
