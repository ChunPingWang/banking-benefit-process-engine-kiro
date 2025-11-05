package com.bank.promotion.domain.command.external;

import java.util.concurrent.TimeUnit;

/**
 * 外部系統適配器介面
 * 定義與外部系統互動的統一介面
 */
public interface ExternalSystemAdapter {
    
    /**
     * 呼叫外部系統
     * 
     * @param request 請求資料
     * @param timeout 超時時間
     * @param timeUnit 時間單位
     * @return 外部系統回應
     * @throws ExternalSystemException 當呼叫失敗時
     */
    ExternalSystemResponse call(ExternalSystemRequest request, long timeout, TimeUnit timeUnit) 
            throws ExternalSystemException;
    
    /**
     * 檢查外部系統是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 獲取適配器類型
     * 
     * @return 適配器類型
     */
    String getAdapterType();
    
    /**
     * 關閉適配器並釋放資源
     */
    void close();
}