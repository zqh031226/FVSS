package com.example.contracTest;

import com.alibaba.fastjson.JSON;
import com.example.smartcontractfvss.FVSS;
import com.example.smartcontractfvss.Participants;
import com.example.smartcontractfvss.ParticipantsContract;
import org.apache.commons.lang3.tuple.Pair;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.math.BigInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class queryParticipantsTest {


    @Test
    public void testQueryParticipant() {

        ChaincodeStub stub = Mockito.mock(ChaincodeStub.class);
        Context ctx = Mockito.mock(Context.class);
        when(ctx.getStub()).thenReturn(stub);

        org.apache.commons.lang3.tuple.Pair<BigInteger[], BigInteger[]> distributeSecret = FVSS.distributeSecret(FVSS.secret);
        BigInteger[] Xi_value = distributeSecret.getLeft();
        BigInteger[] SXi_value = distributeSecret.getRight();
        BigInteger[] commit = new BigInteger[1]; //只新增一个节点的情况，如果新增多个就换成数组，用i遍历
        BigInteger[] verification = new BigInteger[1];

        BigInteger Xi = null;
        BigInteger SXi = null;
        BigInteger Ci = null;
        BigInteger Vi = null;
        for (int i = 0; i < 1; i++) {

            Xi = Xi_value[i];
            SXi = SXi_value[i];

            Ci = FVSS.g.modPow(SXi, FVSS.p);
            Vi = FVSS.hash(Xi.toString() + SXi.toString() + commit.toString());

        }

        //新增一个节点；如果需要新增多条数据，换成数组就行
        String key = "P10";
        int id = 10;
        String name = "P10";
        int assets = 1000;
        int deposit = 100;
        Pair<BigInteger, BigInteger> secretShares = Pair.of(Xi, SXi);
        commit[0] = Ci;
        verification[0] = Vi;

        ParticipantsContract contract = new ParticipantsContract();

        Participants createdParticipant = contract.createParticipants(//调用合约函数createParticipants创建P10节点。
                ctx, key, id, name, assets, deposit, secretShares, commit[0], verification[0]);

        String createParticipant_json = JSON.toJSONString(createdParticipant);
        stub.putStringState(key, createParticipant_json);

        // 模拟对象stub的getStringState方法中返回具有给定密钥的参与者对象
        when(stub.getStringState(key)).thenReturn(createParticipant_json);

        Participants queryParticipants = contract.queryParticipant(ctx, key);//调用合约函数queryParticipant查询P10节点。

        // 验证模拟对象stub的getStringState方法是否以相应的密钥被调用了一次
        verify(stub, times(2)).getStringState(eq(key));
        assertEquals(createdParticipant, queryParticipants);

        System.out.format("节点%s的信息查询结果：\n"+JSON.toJSONString(queryParticipants),key);

    }

}