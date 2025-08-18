package com.turkcellcase4.billing.mapper;

import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.dto.BillItemDTO;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillMapper {
    
    @Mapping(source = "billId", target = "billId")
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "periodStart", target = "periodStart")
    @Mapping(source = "periodEnd", target = "periodEnd")
    @Mapping(source = "issueDate", target = "issueDate")
    @Mapping(source = "totalAmount", target = "totalAmount")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "billItems", target = "items")
    BillResponseDTO toBillResponseDTO(Bill bill);
    
    @Mapping(source = "itemId", target = "itemId")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "subtype", target = "subtype")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "unitPrice", target = "unitPrice")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "taxRate", target = "taxRate")
    BillItemDTO toBillItemDTO(BillItem billItem);
    
    List<BillResponseDTO> toBillResponseDTOList(List<Bill> bills);
    
    List<BillItemDTO> toBillItemDTOList(List<BillItem> billItems);
}
