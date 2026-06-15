-- Seed Product Reviews
-- Only adding reviews for products that users have completed orders for
-- Using realistic Vietnamese reviews with proper ratings and feedback

-- User 01KV05RTFCABHZSQJR4BTNKB4K reviews for their purchased products
INSERT INTO product_reviews (review_id, product_id, user_id, order_id, rating, title, content, image_urls, status, merchant_reply, merchant_replied_at, created_at, updated_at) VALUES
('REV_0001', 'PR_0001', '01KV05RTFCABHZSQJR4BTNKB4K', 'ORD_001', 5, 'Sản phẩm tuyệt vời!', 'Bảng điều khiển rất hiện đại và dễ sử dụng. Chất lượng tốt, giao hàng nhanh. Rất hài lòng với sản phẩm này!', '["https://images.unsplash.com/photo-1565814636199-ae8133055c1c?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', 'Cảm ơn bạn đã tin tưởng sản phẩm của chúng tôi! Chúc bạn sử dụng hiệu quả.', NOW() - INTERVAL '2 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days'),

('REV_0002', 'PR_0005', '01KV05RTFCABHZSQJR4BTNKB4K', 'ORD_001', 4, 'Điện thoại gập rất tốt', 'Màn hình đẹp, gập mở mượt mà. Trừ 1 sao vì pin hơi yếu, nhưng tổng thể rất ổn!', '["https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

('REV_0003', 'PR_0010', '01KV05RTFCABHZSQJR4BTNKB4K', 'ORD_001', 5, 'Dép quá êm!', 'Đi rất thoải mái, không gây đau chân. Chất liệu mềm mại và bền. Sẽ mua thêm!', '[]'::jsonb, 'VISIBLE', 'Rất vui khi bạn hài lòng! Chúng tôi luôn có nhiều màu sắc và kích cỡ để bạn lựa chọn.', NOW() - INTERVAL '1 day', NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day'),

-- User 01KV05RTFD9R88AGKRTYSBYGW8 reviews
('REV_0004', 'PR_0002', '01KV05RTFD9R88AGKRTYSBYGW8', 'ORD_002', 5, 'Áo len cực đẹp', 'Chất vải mềm mại, ấm áp, form áo vừa vặn. Đóng gói cẩn thận. Giá cả hợp lý!', '["https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),

('REV_0005', 'PR_0008', '01KV05RTFD9R88AGKRTYSBYGW8', 'ORD_002', 4, 'Điện thoại nhỏ gọn tiện lợi', 'Màn hình nhỏ nhưng hiển thị sắc nét. Camera tốt, pin trâu. Hơi tiếc là không có sạc nhanh.', '[]'::jsonb, 'VISIBLE', 'Cảm ơn phản hồi của bạn. Dòng sản phẩm mới của chúng tôi sẽ có sạc nhanh!', NOW() - INTERVAL '1 day', NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day'),

('REV_0006', 'PR_0015', '01KV05RTFD9R88AGKRTYSBYGW8', 'ORD_002', 3, 'Tạm ổn', 'Điện thoại dùng bình thường, không có gì đặc biệt. Giá hơi cao so với cấu hình.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

-- User 01KV05RTFEECNWP83Q8D0MX5NR reviews
('REV_0007', 'PR_0003', '01KV05RTFEECNWP83Q8D0MX5NR', 'ORD_003', 5, 'Giày rất đẹp và thoải mái', 'Giày đi êm chân, không bị đau. Màu sắc đẹp như hình. Đóng gói cẩn thận. Sẽ ủng hộ shop lâu dài!', '["https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', 'Cảm ơn bạn rất nhiều! Chúc bạn luôn tự tin và thoải mái.', NOW() - INTERVAL '2 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '2 days'),

('REV_0008', 'PR_0011', '01KV05RTFEECNWP83Q8D0MX5NR', 'ORD_003', 5, 'Bóng đèn thông minh tuyệt vời', 'Điều khiển qua app rất tiện. Ánh sáng mềm mại, điều chỉnh được độ sáng và màu. Giá hợp lý!', '["https://images.unsplash.com/photo-1565814636199-ae8133055c1c?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),

('REV_0009', 'PR_0019', '01KV05RTFEECNWP83Q8D0MX5NR', 'ORD_003', 4, 'Giày thể thao đẹp, chất lượng tốt', 'Giày nhẹ, đi thoải mái. Đế cao su chống trơn tốt. Trừ 1 sao vì size hơi nhỏ so với thông thường.', '[]'::jsonb, 'VISIBLE', 'Xin lỗi vì sự bất tiện! Chúng tôi đã cập nhật bảng size chi tiết hơn trên trang sản phẩm.', NOW() - INTERVAL '1 day', NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day'),

-- User 01KV05RTFFQNP5Z7WS7TV6EZQ3 reviews
('REV_0010', 'PR_0004', '01KV05RTFFQNP5Z7WS7TV6EZQ3', 'ORD_004', 5, 'Áo thun chất lượng cao', 'Vải cotton mềm mại, thấm hút mồ hôi tốt. Form áo đẹp, không bị nhăn sau khi giặt. Đáng tiền!', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),

('REV_0011', 'PR_0012', '01KV05RTFFQNP5Z7WS7TV6EZQ3', 'ORD_004', 5, 'Kẹo dẻo vitamin ngon', 'Hương vị thơm ngon, không bị ngấy. Con em rất thích. Bổ sung vitamin hàng ngày rất tiện!', '["https://images.unsplash.com/photo-1584017911766-d451b3d0e843?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', 'Cảm ơn bạn! Chúc gia đình bạn luôn khỏe mạnh.', NOW() - INTERVAL '3 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '3 days'),

('REV_0012', 'PR_0020', '01KV05RTFFQNP5Z7WS7TV6EZQ3', 'ORD_004', 4, 'Điện thoại giá rẻ, chất lượng tốt', 'Màn hình hiển thị sáng, pin trâu. Cấu hình đủ dùng cho nhu cầu cơ bản. Tốt cho người già.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),

-- User 01KV05RTFGD8DGY2MA0D2SASFB reviews
('REV_0013', 'PR_0006', '01KV05RTFGD8DGY2MA0D2SASFB', 'ORD_005', 5, 'Váy đẹp, chất vải tốt', 'Váy mặc rất duyên dáng, vải mát. Màu sắc đúng như hình. Rất hài lòng với lần mua này!', '["https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),

('REV_0014', 'PR_0013', '01KV05RTFGD8DGY2MA0D2SASFB', 'ORD_005', 5, 'Áo khoác đẹp và ấm', 'Chất liệu dày dặn, giữ ấm tốt. Thiết kế thời trang. Rất đáng mua!', '[]'::jsonb, 'VISIBLE', 'Cảm ơn bạn đã lựa chọn sản phẩm của chúng tôi!', NOW() - INTERVAL '2 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '2 days'),

('REV_0015', 'PR_0024', '01KV05RTFGD8DGY2MA0D2SASFB', 'ORD_005', 4, 'Củ sạc nhỏ gọn, tiện lợi', 'Sạc nhanh, nhỏ gọn dễ mang đi. Hơi nóng khi sạc nhưng vẫn chấp nhận được.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),

-- User 01KV05RTFHK15HJ1HVAWCCC0DM reviews
('REV_0016', 'PR_0007', '01KV05RTFHK15HJ1HVAWCCC0DM', 'ORD_006', 4, 'Áo khoác chất lượng', 'Vải dày, may đẹp. Phù hợp mùa đông. Trừ 1 sao vì màu hơi nhạt hơn ảnh.', '[]'::jsonb, 'VISIBLE', 'Xin lỗi về sự khác biệt màu sắc. Chúng tôi đã cập nhật ảnh chụp thật hơn.', NOW() - INTERVAL '1 day', NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day'),

('REV_0017', 'PR_0014', '01KV05RTFHK15HJ1HVAWCCC0DM', 'ORD_006', 5, 'Ghế ngồi rất êm', 'Ghế thiết kế đẹp, ngồi thoải mái. Chất liệu bền. Rất phù hợp cho văn phòng!', '["https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),

('REV_0018', 'PR_0025', '01KV05RTFHK15HJ1HVAWCCC0DM', 'ORD_006', 5, 'Giá đỡ chắc chắn, thiết kế đẹp', 'Giá đỡ làm bằng kim loại chắc chắn. Thiết kế tối giản, sang trọng. Đáng đồng tiền!', '[]'::jsonb, 'VISIBLE', 'Cảm ơn bạn đã tin tưởng chất lượng sản phẩm!', NOW() - INTERVAL '3 days', NOW() - INTERVAL '8 days', NOW() - INTERVAL '3 days'),

-- User 01KV05RTFJQ2FMYHJV9DKAX76J reviews
('REV_0019', 'PR_0009', '01KV05RTFJQ2FMYHJV9DKAX76J', 'ORD_007', 5, 'Laptop mạnh mẽ', 'Cấu hình cao, chạy mượt mà. Thiết kế đẹp, màn hình sắc nét. Rất phù hợp cho công việc!', '["https://images.unsplash.com/photo-1531297484001-80022131f5a1?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),

('REV_0020', 'PR_0016', '01KV05RTFJQ2FMYHJV9DKAX76J', 'ORD_007', 4, 'Áo khoác đẹp nhưng hơi mỏng', 'Thiết kế đẹp, vải mềm mại. Nhưng hơi mỏng, không phù hợp trời rất lạnh.', '[]'::jsonb, 'VISIBLE', 'Cảm ơn góp ý! Đây là dòng áo khoác mùa thu, chúng tôi có dòng áo khoác mùa đông dày hơn.', NOW() - INTERVAL '2 days', NOW() - INTERVAL '10 days', NOW() - INTERVAL '2 days'),

('REV_0021', 'PR_0029', '01KV05RTFJQ2FMYHJV9DKAX76J', 'ORD_007', 5, 'Bộ điều nhiệt thông minh tuyệt vời', 'Điều khiển từ xa qua app rất tiện. Tiết kiệm điện. Lắp đặt dễ dàng. Rất hài lòng!', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),

-- User 01KV05RTFKGZEB4QJ0QAR56P0K reviews
('REV_0022', 'PR_0017', '01KV05RTFKGZEB4QJ0QAR56P0K', 'ORD_008', 5, 'Nồi cơm điện chất lượng cao', 'Nấu cơm rất ngon, chín đều. Dễ sử dụng, dễ vệ sinh. Giá hợp lý!', '["https://images.unsplash.com/photo-1588854337236-6889d631faa8?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', 'Cảm ơn bạn! Chúc gia đình bạn bữa cơm ngon miệng!', NOW() - INTERVAL '4 days', NOW() - INTERVAL '12 days', NOW() - INTERVAL '4 days'),

('REV_0023', 'PR_0018', '01KV05RTFKGZEB4QJ0QAR56P0K', 'ORD_008', 5, 'Dải đèn LED đẹp', 'Ánh sáng màu sắc đa dạng. Điều khiển qua remote tiện lợi. Dán dính chắc chắn.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),

('REV_0024', 'PR_0030', '01KV05RTFKGZEB4QJ0QAR56P0K', 'ORD_008', 4, 'Bếp nướng nhỏ gọn, tiện dụng', 'Nướng nhanh, nhiệt độ đều. Dễ vệ sinh. Hơi nhỏ cho gia đình đông người.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),

-- User 01KV05RTFMGMD0RKM204JGX80N reviews
('REV_0025', 'PR_0021', '01KV05RTFMGMD0RKM204JGX80N', 'ORD_009', 5, 'Áo khoác thiết kế đẹp', 'Chất vải cao cấp, form áo đẹp. Mặc lên rất sang trọng. Giá có cao nhưng đáng tiền!', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),

('REV_0026', 'PR_0022', '01KV05RTFMGMD0RKM204JGX80N', 'ORD_009', 5, 'Viên nang tốt cho sức khỏe', 'Dùng được 2 tuần thấy cơ thể khỏe hơn. Không bị tác dụng phụ. Sẽ mua tiếp!', '["https://images.unsplash.com/photo-1611070973770-b1a60c268ab1?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', 'Cảm ơn bạn! Chúc bạn luôn dồi dào sức khỏe.', NOW() - INTERVAL '5 days', NOW() - INTERVAL '12 days', NOW() - INTERVAL '5 days'),

('REV_0027', 'PR_0031', '01KV05RTFMGMD0RKM204JGX80N', 'ORD_009', 4, 'Tai nghe chất lượng âm thanh tốt', 'Âm thanh trong trẻo, bass đậm. Pin trâu. Hơi khó kết nối bluetooth ban đầu.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '11 days', NOW() - INTERVAL '11 days'),

-- User 01KV05RTFNH4AW0JQG3MGZAGRK reviews
('REV_0028', 'PR_0023', '01KV05RTFNH4AW0JQG3MGZAGRK', 'ORD_010', 5, 'Laptop hiệu năng cao', 'Cấu hình mạnh, chạy phần mềm nặng mượt mà. Thiết kế mỏng nhẹ. Rất đáng mua!', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),

('REV_0029', 'PR_0026', '01KV05RTFNH4AW0JQG3MGZAGRK', 'ORD_010', 5, 'Kẹo dẻo vitamin cho cả gia đình', 'Cả nhà đều thích dùng. Hương vị thơm ngon. Bổ sung vitamin hàng ngày rất tiện!', '[]'::jsonb, 'VISIBLE', 'Rất vui khi cả gia đình bạn hài lòng!', NOW() - INTERVAL '6 days', NOW() - INTERVAL '13 days', NOW() - INTERVAL '6 days'),

('REV_0030', 'PR_0033', '01KV05RTFNH4AW0JQG3MGZAGRK', 'ORD_010', 5, 'Camera an ninh chất lượng cao', 'Hình ảnh sắc nét cả ngày lẫn đêm. App điều khiển dễ dùng. Lắp đặt đơn giản!', '["https://images.unsplash.com/photo-1562408590-e32931084e23?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),

-- More diverse reviews with different ratings
('REV_0031', 'PR_0027', '01KV05RTFPWNS3N0WXRWQSFQ4T', 'ORD_011', 3, 'Áo len bình thường', 'Chất vải tạm ổn, không mềm lắm. Giá hơi cao so với chất lượng.', '[]'::jsonb, 'VISIBLE', 'Xin lỗi vì không đáp ứng kỳ vọng của bạn. Chúng tôi sẽ cải thiện chất lượng sản phẩm.', NOW() - INTERVAL '3 days', NOW() - INTERVAL '15 days', NOW() - INTERVAL '3 days'),

('REV_0032', 'PR_0028', '01KV05RTFPWNS3N0WXRWQSFQ4T', 'ORD_011', 5, 'Thực phẩm bổ sung hiệu quả', 'Dùng được 3 tuần thấy sức khỏe tốt hơn. Dễ uống. Giá cả hợp lý!', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),

('REV_0033', 'PR_0034', '01KV05RTFPWNS3N0WXRWQSFQ4T', 'ORD_011', 4, 'Đồng hồ thông minh đẹp', 'Thiết kế đẹp, màn hình sắc nét. Nhiều tính năng hữu ích. Pin hơi yếu.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days'),

('REV_0034', 'PR_0032', '01KV05RTFQN0617KQBQP29S3KR', 'ORD_012', 4, 'Vitamin tốt', 'Sản phẩm chất lượng, bổ sung vitamin đầy đủ. Hơi đắt.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '16 days', NOW() - INTERVAL '16 days'),

('REV_0035', 'PR_0035', '01KV05RTFQN0617KQBQP29S3KR', 'ORD_012', 5, 'Củ sạc nhỏ gọn tiện lợi', 'Sạc nhanh, nhỏ gọn dễ mang theo. Chất lượng tốt. Giá hợp lý!', '[]'::jsonb, 'VISIBLE', 'Cảm ơn bạn đã tin tưởng sản phẩm!', NOW() - INTERVAL '7 days', NOW() - INTERVAL '15 days', NOW() - INTERVAL '7 days'),

('REV_0036', 'PR_0036', '01KV05RTFQN0617KQBQP29S3KR', 'ORD_012', 5, 'Giày bốt đẹp và bền', 'Chất liệu da tốt, đi êm chân. Thiết kế thời trang. Rất đáng mua!', '["https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80"]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),

('REV_0037', 'PR_0037', '01KV05RTFRFE6Q2WDK8D48SGQD', 'ORD_013', 4, 'Áo sơ mi chất lượng tốt', 'Vải mịn, may đẹp. Form áo vừa vặn. Hơi dễ nhăn.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '17 days', NOW() - INTERVAL '17 days'),

('REV_0038', 'PR_0038', '01KV05RTFRFE6Q2WDK8D48SGQD', 'ORD_013', 5, 'Laptop văn phòng tốt', 'Cấu hình đủ dùng cho công việc văn phòng. Pin trâu. Giá cả phải chăng!', '[]'::jsonb, 'VISIBLE', 'Cảm ơn bạn! Chúc bạn làm việc hiệu quả.', NOW() - INTERVAL '8 days', NOW() - INTERVAL '16 days', NOW() - INTERVAL '8 days'),

('REV_0039', 'PR_0039', '01KV05RTFRFE6Q2WDK8D48SGQD', 'ORD_013', 5, 'Camera an ninh đáng tin cậy', 'Hình ảnh rõ nét, ghi hình liên tục. Cảnh báo chuyển động chính xác. Tuyệt vời!', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),

('REV_0040', 'PR_0001', '01KV05RTFSN9NDA92Y53Y6CZAB', 'ORD_014', 4, 'Bảng điều khiển hiện đại', 'Giao diện đẹp, dễ sử dụng. Tích hợp nhiều tính năng. Hướng dẫn hơi ít.', '[]'::jsonb, 'VISIBLE', NULL, NULL, NOW() - INTERVAL '18 days', NOW() - INTERVAL '18 days');

