package com.capstone.exff.services;

import com.capstone.exff.entities.TransactionEntity;
import com.capstone.exff.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class TransactionServicesImpl implements TransactionServices {

    private TransactionRepository transactionRepository;

    @Autowired
    public TransactionServicesImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public TransactionEntity createTransaction(int senderId, int receiverId, int donationId,
                                               String status, Timestamp createTime, Timestamp modifiedTime) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setSenderId(senderId);
        transaction.setReceiverId(receiverId);
        transaction.setDonationPostId(donationId);
        transaction.setStatus(status);
        transaction.setCreateTime(createTime);
        transaction.setModifyTime(modifiedTime);
        return transactionRepository.save(transaction);
    }
}