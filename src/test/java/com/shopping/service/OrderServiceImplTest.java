package com.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shopping.constant.OrderStatus;
import com.shopping.dto.request.OrderCreateRequest;
import com.shopping.dto.request.OrderItemRequest;
import com.shopping.dto.request.OrderPayRequest;
import com.shopping.dto.response.OrderItemResponse;
import com.shopping.dto.response.OrderResponse;
import com.shopping.entity.*;
import com.shopping.exception.BusinessException;
import com.shopping.exception.ErrorCode;
import com.shopping.mapper.OrdersMapper;
import com.shopping.utils.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private OrdersMapper ordersMapper;

    @Mock
    private UserService userService;

    @Mock
    private MerchantService merchantService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductSkuService productSkuService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private IdGenerator idGenerator;

    // === 被测类：真实实例，注入 mocks ===
    @InjectMocks
    private OrderServiceImpl orderService; // 继承了 MyBatis-Plus ServiceImpl

    // === 测试数据 ===
    private User user;
    private Merchant merchant;
    private Product product;
    private ProductSku sku;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setUsername("testUser");
        user.setStatus(1);

        merchant = new Merchant();
        merchant.setMerchantId(100L);
        merchant.setUserId(2L);
        merchant.setStatus(1);

        product = new Product();
        product.setProductId(10L);
        product.setMerchantId(100L);
        product.setStatus(1);

        sku = new ProductSku();
        sku.setSkuId(1000L);
        sku.setProductId(10L);
        sku.setPrice(new BigDecimal("100.00"));
        sku.setStockQuantity(10);
        sku.setStockVersion(1L);
        sku.setStatus(1);

        order = new Order();
        order.setOrderId(10000L);
        order.setOrderNo("ORD20251231001");
        order.setUserId(1L);
        order.setMerchantId(100L);
        order.setTotalAmount(new BigDecimal("200.00"));
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setCreatedTime(LocalDateTime.now());
    }

    // ==================== createOrder ====================

    @Test
    void createOrder_success() {
        // 构建请求（分步赋值）
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setSkuId(1000L);
        itemReq.setQuantity(2);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        request.setOrderItems(Collections.singletonList(itemReq));

        when(userService.getUserById(1L)).thenReturn(user);
        when(merchantService.findById(100L)).thenReturn(merchant);
        when(productSkuService.validateStock(1000L, 2)).thenReturn(sku);
        when(productService.findById(10L)).thenReturn(product);
        when(idGenerator.generateOrderNo()).thenReturn("ORD20251231001");

        doAnswer(invocation -> {
            Order arg = invocation.getArgument(0);
            arg.setOrderId(10000L);
            return 1;
        }).when(ordersMapper).insert(any(Order.class));

        when(productSkuService.subtractStock(eq(1000L), eq(2), eq(1L), eq(10000L), eq("createOrder"), anyString()))
                .thenReturn(true);

        when(orderItemService.saveBatch(anyList())).thenReturn(true);

        // 调用被测方法（真实逻辑）
        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("ORD20251231001", response.getOrderNo());
        assertEquals(new BigDecimal("200.00"), response.getTotalAmount());

        verify(ordersMapper, times(1)).insert(any(Order.class));
        verify(orderItemService, times(1)).saveBatch(anyList());
    }

    @Test
    void createOrder_userDisabled_throwsException() {
        user.setStatus(0);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);
        request.setMerchantId(100L);
        request.setOrderItems(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.createOrder(request));
        assertEquals(40000, ex.getCode());
    }

    // ==================== payOrder ====================

    @Test
    void payOrder_success() {
        when(ordersMapper.selectById(10000L)).thenReturn(order);
        when(merchantService.findById(100L)).thenReturn(merchant);

        when(userService.updateUserBalance(eq(1L), eq(new BigDecimal("200.00")), eq("decrease"), eq(10000L), eq(2), anyString()))
                .thenReturn(new User());

        when(merchantService.updateBalance(eq(100L), eq(new BigDecimal("200.00")), eq(true), eq(10000L), eq(3), anyString()))
                .thenReturn(true);

        when(ordersMapper.updateOrderPaid(10000L)).thenReturn(1);

        OrderPayRequest request = new OrderPayRequest();
        request.setOrderId(10000L);

        OrderResponse response = orderService.payOrder(request);

        assertNotNull(response);
        assertEquals(OrderStatus.PAID.getCode(), response.getOrderStatus());
        assertNotNull(response.getPaymentTime());
    }

    // ==================== findDetailById ====================

    @Test
    void findDetailById_success() {
        when(ordersMapper.selectById(10000L)).thenReturn(order);

        OrderItem item = new OrderItem();
        item.setItemId(1L);
        item.setOrderId(10000L);
        item.setSkuId(1000L);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setTotalPrice(new BigDecimal("200.00"));

        when(orderItemService.findByOrderId(10000L)).thenReturn(Collections.singletonList(item));
        when(userService.getUserById(1L)).thenReturn(user);
        when(merchantService.findById(100L)).thenReturn(merchant);
        when(productSkuService.findById(1000L)).thenReturn(sku);

        OrderResponse response = orderService.findDetailById(10000L);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        OrderItemResponse itemResp = response.getItems().get(0);
        assertEquals(1000L, itemResp.getSkuId());
        assertEquals(2, itemResp.getQuantity());
    }

    // ==================== deleteOrder ====================

    @Test
    void deleteOrder_pending_success() {
        // 让 getById 返回 pending 订单
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.getCode());
        when(ordersMapper.selectById(10000L)).thenReturn(order);

        when(orderItemService.remove(any(QueryWrapper.class))).thenReturn(true);
        when(ordersMapper.deleteById(10000L)).thenReturn(1); // 返回影响行数

        boolean result = orderService.deleteOrder(10000L);

        assertTrue(result);
        verify(ordersMapper, times(1)).deleteById(10000L);
        verify(orderItemService, times(1)).remove(any(QueryWrapper.class));
    }

    @Test
    void deleteOrder_paid_cannotDelete() {
        order.setOrderStatus(OrderStatus.PAID.getCode());
        when(ordersMapper.selectById(10000L)).thenReturn(order);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.deleteOrder(10000L));
        assertEquals(ErrorCode.ORDER_STATUS_ERROR.getCode(), ex.getCode());
    }

    // ==================== findPage ====================

    @Test
    void findPage_success() {
        Page<Order> page = new Page<>(1, 10);
        Page<Order> mockResult = new Page<>();
        mockResult.setRecords(Collections.singletonList(order));
        mockResult.setTotal(1);

        when(ordersMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(mockResult);

        Page<Order> result = orderService.findPage(1, 10, 1L, 100L, null);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    // ==================== calculateOrderAmount ====================

    @Test
    void calculateOrderAmount_success() {
        when(ordersMapper.selectById(10000L)).thenReturn(order);
        BigDecimal amount = orderService.calculateOrderAmount(10000L);
        assertEquals(new BigDecimal("200.00"), amount);
    }

    @Test
    void calculateOrderAmount_notFound() {
        when(ordersMapper.selectById(999L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.calculateOrderAmount(999L));
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), ex.getCode());
    }
}