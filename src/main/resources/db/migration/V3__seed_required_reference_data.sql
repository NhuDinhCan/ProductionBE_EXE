IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER') INSERT INTO roles(name) VALUES ('USER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'MENTOR') INSERT INTO roles(name) VALUES ('MENTOR');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN') INSERT INTO roles(name) VALUES ('ADMIN');

IF NOT EXISTS (SELECT 1 FROM career WHERE name = N'Công nghệ thông tin') INSERT INTO career(name) VALUES (N'Công nghệ thông tin');
IF NOT EXISTS (SELECT 1 FROM career WHERE name = N'Kinh tế') INSERT INTO career(name) VALUES (N'Kinh tế');
IF NOT EXISTS (SELECT 1 FROM career WHERE name = N'Marketing') INSERT INTO career(name) VALUES (N'Marketing');
IF NOT EXISTS (SELECT 1 FROM career WHERE name = N'Thiết kế đồ họa') INSERT INTO career(name) VALUES (N'Thiết kế đồ họa');
IF NOT EXISTS (SELECT 1 FROM career WHERE name = N'Ngôn ngữ Anh') INSERT INTO career(name) VALUES (N'Ngôn ngữ Anh');
IF NOT EXISTS (SELECT 1 FROM career WHERE name = N'Y dược') INSERT INTO career(name) VALUES (N'Y dược');
IF NOT EXISTS (SELECT 1 FROM career WHERE name = N'Kỹ thuật') INSERT INTO career(name) VALUES (N'Kỹ thuật');
IF NOT EXISTS (SELECT 1 FROM career WHERE name = N'Luật') INSERT INTO career(name) VALUES (N'Luật');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Project-Based Learning')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Project-Based Learning', N'Học bằng cách làm project, case study hoặc sản phẩm nhỏ theo đúng ngành.', N'Hiểu sâu kiến thức|Có minh chứng năng lực');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Active Recall')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Active Recall', N'Tự kiểm tra lại kiến thức thay vì chỉ đọc lại lý thuyết.', N'Nhớ lâu|Phát hiện lỗ hổng kiến thức');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Feedback Loop')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Feedback Loop', N'Nhận góp ý, chỉnh sửa và ghi lại bài học sau mỗi vòng thực hành.', N'Tiến bộ nhanh|Rèn tiêu chuẩn nghề nghiệp');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Case Study Analysis')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Case Study Analysis', N'Phân tích tình huống thực tế để hiểu bối cảnh, vấn đề và cách ra quyết định.', N'Hiểu thực tế|Rèn tư duy phản biện');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Spaced Repetition')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Spaced Repetition', N'Ôn kiến thức theo chu kỳ bằng flashcard, ví dụ và bài tự kiểm tra.', N'Nhớ lâu|Phù hợp lượng kiến thức lớn');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Design Deconstruction')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Design Deconstruction', N'Phân tích sản phẩm tốt để học layout, màu sắc, cấu trúc và tiêu chuẩn nghề.', N'Luyện mắt thẩm mỹ|Hiểu nguyên tắc ứng dụng');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Shadowing')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Shadowing', N'Nghe và nhại lại đoạn nói ngắn để luyện phát âm, nhịp và ngữ điệu.', N'Cải thiện speaking|Nghe tự nhiên hơn');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Clinical Case Practice')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Clinical Case Practice', N'Học qua ca bệnh giả lập để gắn lý thuyết với xử lý thực tế.', N'Tăng tư duy lâm sàng|Biết ưu tiên thông tin');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'Simulation Learning')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'Simulation Learning', N'Dùng mô phỏng để quan sát quan hệ giữa biến số, công thức và kết quả.', N'Hiểu trực quan|Dễ nhớ công thức');

IF NOT EXISTS (SELECT 1 FROM learning_methods WHERE title = N'IRAC Method')
    INSERT INTO learning_methods(title, description, benefits)
    VALUES (N'IRAC Method', N'Phân tích vấn đề pháp lý theo Issue, Rule, Application và Conclusion.', N'Lập luận rõ|Viết bài chặt chẽ');

DECLARE @it BIGINT = (SELECT TOP 1 id FROM career WHERE name = N'Công nghệ thông tin');
DECLARE @eco BIGINT = (SELECT TOP 1 id FROM career WHERE name = N'Kinh tế');
DECLARE @marketing BIGINT = (SELECT TOP 1 id FROM career WHERE name = N'Marketing');
DECLARE @design BIGINT = (SELECT TOP 1 id FROM career WHERE name = N'Thiết kế đồ họa');
DECLARE @english BIGINT = (SELECT TOP 1 id FROM career WHERE name = N'Ngôn ngữ Anh');
DECLARE @medicine BIGINT = (SELECT TOP 1 id FROM career WHERE name = N'Y dược');
DECLARE @engineering BIGINT = (SELECT TOP 1 id FROM career WHERE name = N'Kỹ thuật');
DECLARE @law BIGINT = (SELECT TOP 1 id FROM career WHERE name = N'Luật');

DECLARE @project BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Project-Based Learning');
DECLARE @active BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Active Recall');
DECLARE @feedback BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Feedback Loop');
DECLARE @case BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Case Study Analysis');
DECLARE @spaced BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Spaced Repetition');
DECLARE @designMethod BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Design Deconstruction');
DECLARE @shadowing BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Shadowing');
DECLARE @clinical BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Clinical Case Practice');
DECLARE @simulation BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'Simulation Learning');
DECLARE @irac BIGINT = (SELECT TOP 1 id FROM learning_methods WHERE title = N'IRAC Method');

IF @it IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_strategy_profiles WHERE career_id = @it)
    INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
    VALUES (@it, N'Ngành IT cần học theo hướng thực hành, làm project đều đặn và rèn tư duy logic qua bài toán thật.', N'Tư duy logic|Debug|Database|Git|API|Testing', N'IntelliJ IDEA|VS Code|GitHub|Postman|Docker|LeetCode', N'Tuần 1: Ôn nền tảng lập trình và Git|Tuần 2: Học database, REST API và authentication|Tuần 3: Làm mini project fullstack|Tuần 4: Viết test, deploy thử và hoàn thiện README');

IF @eco IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_strategy_profiles WHERE career_id = @eco)
    INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
    VALUES (@eco, N'Ngành Kinh tế nên học bằng case study, dữ liệu thực tế và luyện khả năng phân tích nguyên nhân - tác động.', N'Phân tích số liệu|Tư duy phản biện|Excel|Đọc báo cáo|Thuyết trình', N'Excel|Google Sheets|World Bank Data|Trading Economics|Notion', N'Tuần 1: Ôn cung cầu và thị trường|Tuần 2: Đọc case study doanh nghiệp|Tuần 3: Phân tích bộ dữ liệu nhỏ|Tuần 4: Viết báo cáo và trình bày kết luận');

IF @marketing IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_strategy_profiles WHERE career_id = @marketing)
    INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
    VALUES (@marketing, N'Marketing cần học qua chiến dịch thật, nghiên cứu khách hàng và đo lường hiệu quả bằng dữ liệu.', N'Customer insight|Content|Phân tích dữ liệu|Branding|Presentation', N'Canva|Meta Ads Library|Google Trends|Google Analytics|Notion', N'Tuần 1: Học STP, 4P và persona|Tuần 2: Phân tích chiến dịch mẫu|Tuần 3: Viết content plan|Tuần 4: Tạo mini campaign và đo chỉ số');

IF @design IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_strategy_profiles WHERE career_id = @design)
    INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
    VALUES (@design, N'Thiết kế đồ họa nên học qua portfolio, luyện mắt thẩm mỹ và nhận feedback thường xuyên.', N'Typography|Layout|Màu sắc|Brand identity|Visual storytelling', N'Figma|Adobe Illustrator|Photoshop|Behance|Pinterest', N'Tuần 1: Ôn bố cục, màu sắc và typography|Tuần 2: Copywork thiết kế tốt|Tuần 3: Làm bộ poster/social post|Tuần 4: Hoàn thiện portfolio mini');

IF @english IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_strategy_profiles WHERE career_id = @english)
    INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
    VALUES (@english, N'Ngôn ngữ Anh cần học đều 4 kỹ năng, tăng input chất lượng và luyện output có phản hồi.', N'Listening|Speaking|Academic writing|Translation|Presentation', N'Anki|Oxford Learner''s Dictionaries|BBC Learning English|Grammarly|YouGlish', N'Tuần 1: Đánh giá trình độ và lập lịch học|Tuần 2: Shadowing và học từ vựng theo chủ đề|Tuần 3: Viết essay ngắn và sửa lỗi|Tuần 4: Thuyết trình 3 phút và tự đánh giá');

IF @medicine IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_strategy_profiles WHERE career_id = @medicine)
    INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
    VALUES (@medicine, N'Y dược cần học chắc nền tảng, ôn lặp lại theo hệ thống và liên hệ kiến thức với tình huống lâm sàng.', N'Ghi nhớ hệ thống|Phân tích triệu chứng|Đọc tài liệu y khoa|Kỷ luật học tập', N'Anki|AMBOSS|PubMed|Visible Body|Notion', N'Tuần 1: Ôn giải phẫu/sinh lý nền tảng|Tuần 2: Tạo flashcard thuật ngữ và cơ chế|Tuần 3: Làm case study đơn giản|Tuần 4: Tổng ôn và sửa lỗ hổng kiến thức');

IF @engineering IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_strategy_profiles WHERE career_id = @engineering)
    INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
    VALUES (@engineering, N'Kỹ thuật nên học bằng bài toán, mô phỏng và prototype để chuyển công thức thành năng lực giải quyết vấn đề.', N'Toán ứng dụng|Mô hình hóa|Đọc bản vẽ|Phân tích lỗi|Làm việc nhóm', N'MATLAB|AutoCAD|SolidWorks|Excel|PhET Simulations', N'Tuần 1: Ôn toán, vật lý và công thức nền|Tuần 2: Giải bài tập theo từng dạng|Tuần 3: Mô phỏng bài toán kỹ thuật nhỏ|Tuần 4: Làm prototype hoặc báo cáo giải pháp');

IF @law IS NOT NULL AND NOT EXISTS (SELECT 1 FROM learning_strategy_profiles WHERE career_id = @law)
    INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
    VALUES (@law, N'Ngành Luật cần học qua văn bản pháp luật, tình huống và luyện lập luận có căn cứ.', N'Đọc hiểu văn bản pháp luật|Lập luận|Phản biện|Viết pháp lý|Tra cứu', N'Thư viện Pháp luật|Google Scholar|Notion|Zotero|Microsoft Word', N'Tuần 1: Ôn cấu trúc văn bản và thuật ngữ|Tuần 2: Đọc tình huống và viết brief|Tuần 3: Luyện IRAC cho bài tập|Tuần 4: Tranh luận nhóm và viết memo ngắn');

IF @it IS NOT NULL AND @project IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@it AND learning_method_id=@project) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@it, @project, 1);
IF @it IS NOT NULL AND @active IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@it AND learning_method_id=@active) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@it, @active, 2);
IF @it IS NOT NULL AND @feedback IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@it AND learning_method_id=@feedback) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@it, @feedback, 3);

IF @eco IS NOT NULL AND @case IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@eco AND learning_method_id=@case) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@eco, @case, 1);
IF @eco IS NOT NULL AND @active IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@eco AND learning_method_id=@active) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@eco, @active, 2);
IF @eco IS NOT NULL AND @feedback IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@eco AND learning_method_id=@feedback) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@eco, @feedback, 3);

IF @marketing IS NOT NULL AND @case IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@marketing AND learning_method_id=@case) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@marketing, @case, 1);
IF @marketing IS NOT NULL AND @project IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@marketing AND learning_method_id=@project) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@marketing, @project, 2);
IF @marketing IS NOT NULL AND @feedback IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@marketing AND learning_method_id=@feedback) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@marketing, @feedback, 3);

IF @design IS NOT NULL AND @designMethod IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@design AND learning_method_id=@designMethod) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@design, @designMethod, 1);
IF @design IS NOT NULL AND @project IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@design AND learning_method_id=@project) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@design, @project, 2);
IF @design IS NOT NULL AND @feedback IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@design AND learning_method_id=@feedback) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@design, @feedback, 3);

IF @english IS NOT NULL AND @shadowing IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@english AND learning_method_id=@shadowing) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@english, @shadowing, 1);
IF @english IS NOT NULL AND @spaced IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@english AND learning_method_id=@spaced) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@english, @spaced, 2);
IF @english IS NOT NULL AND @feedback IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@english AND learning_method_id=@feedback) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@english, @feedback, 3);

IF @medicine IS NOT NULL AND @clinical IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@medicine AND learning_method_id=@clinical) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@medicine, @clinical, 1);
IF @medicine IS NOT NULL AND @spaced IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@medicine AND learning_method_id=@spaced) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@medicine, @spaced, 2);
IF @medicine IS NOT NULL AND @active IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@medicine AND learning_method_id=@active) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@medicine, @active, 3);

IF @engineering IS NOT NULL AND @simulation IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@engineering AND learning_method_id=@simulation) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@engineering, @simulation, 1);
IF @engineering IS NOT NULL AND @project IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@engineering AND learning_method_id=@project) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@engineering, @project, 2);
IF @engineering IS NOT NULL AND @feedback IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@engineering AND learning_method_id=@feedback) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@engineering, @feedback, 3);

IF @law IS NOT NULL AND @irac IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@law AND learning_method_id=@irac) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@law, @irac, 1);
IF @law IS NOT NULL AND @case IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@law AND learning_method_id=@case) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@law, @case, 2);
IF @law IS NOT NULL AND @feedback IS NOT NULL AND NOT EXISTS (SELECT 1 FROM major_learning_methods WHERE career_id=@law AND learning_method_id=@feedback) INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order) VALUES (@law, @feedback, 3);
