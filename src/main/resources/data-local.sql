INSERT INTO roles(name) VALUES ('USER');
INSERT INTO roles(name) VALUES ('MENTOR');
INSERT INTO roles(name) VALUES ('ADMIN');

INSERT INTO career(name) VALUES ('Công nghệ thông tin');
INSERT INTO career(name) VALUES ('Kinh tế');
INSERT INTO career(name) VALUES ('Marketing');

INSERT INTO learning_methods(title, description, benefits)
VALUES ('Project-Based Learning', 'Học bằng cách làm project, case study hoặc sản phẩm nhỏ theo đúng ngành.', 'Hiểu sâu kiến thức|Có minh chứng năng lực');

INSERT INTO learning_methods(title, description, benefits)
VALUES ('Active Recall', 'Tự kiểm tra lại kiến thức thay vì chỉ đọc lại lý thuyết.', 'Nhớ lâu|Phát hiện lỗ hổng kiến thức');

INSERT INTO learning_methods(title, description, benefits)
VALUES ('Feedback Loop', 'Nhận góp ý, chỉnh sửa và ghi lại bài học sau mỗi vòng thực hành.', 'Tiến bộ nhanh|Rèn tiêu chuẩn nghề nghiệp');

INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
SELECT c.id,
       'Ngành IT nên học theo hướng thực hành, làm project đều đặn và rèn tư duy logic qua bài toán thật.',
       'Tư duy logic|Debug|Database|Git|API|Testing',
       'IntelliJ IDEA|VS Code|GitHub|Postman|Docker|LeetCode',
       'Tuần 1: Ôn nền tảng lập trình và Git|Tuần 2: Học database, REST API và authentication|Tuần 3: Làm mini project fullstack|Tuần 4: Viết test, deploy thử và hoàn thiện README'
FROM career c
WHERE c.name = 'Công nghệ thông tin';

INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
SELECT c.id,
       'Ngành Kinh tế nên học bằng case study, dữ liệu thực tế và luyện khả năng phân tích nguyên nhân - tác động.',
       'Phân tích số liệu|Tư duy phản biện|Excel|Đọc báo cáo|Thuyết trình',
       'Excel|Google Sheets|World Bank Data|Trading Economics|Notion',
       'Tuần 1: Ôn cung cầu và thị trường|Tuần 2: Đọc case study doanh nghiệp|Tuần 3: Phân tích bộ dữ liệu nhỏ|Tuần 4: Viết báo cáo và trình bày kết luận'
FROM career c
WHERE c.name = 'Kinh tế';

INSERT INTO learning_strategy_profiles(career_id, description, skills, tools, weekly_roadmap)
SELECT c.id,
       'Marketing cần học qua chiến dịch thật, nghiên cứu khách hàng và đo lường hiệu quả bằng dữ liệu.',
       'Customer insight|Content|Phân tích dữ liệu|Branding|Presentation',
       'Canva|Meta Ads Library|Google Trends|Google Analytics|Notion',
       'Tuần 1: Học STP, 4P và persona|Tuần 2: Phân tích chiến dịch mẫu|Tuần 3: Viết content plan|Tuần 4: Tạo mini campaign và đo chỉ số'
FROM career c
WHERE c.name = 'Marketing';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 1
FROM career c
JOIN learning_methods m ON m.title = 'Project-Based Learning'
WHERE c.name = 'Công nghệ thông tin';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 2
FROM career c
JOIN learning_methods m ON m.title = 'Active Recall'
WHERE c.name = 'Công nghệ thông tin';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 3
FROM career c
JOIN learning_methods m ON m.title = 'Feedback Loop'
WHERE c.name = 'Công nghệ thông tin';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 1
FROM career c
JOIN learning_methods m ON m.title = 'Project-Based Learning'
WHERE c.name = 'Kinh tế';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 2
FROM career c
JOIN learning_methods m ON m.title = 'Active Recall'
WHERE c.name = 'Kinh tế';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 3
FROM career c
JOIN learning_methods m ON m.title = 'Feedback Loop'
WHERE c.name = 'Kinh tế';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 1
FROM career c
JOIN learning_methods m ON m.title = 'Project-Based Learning'
WHERE c.name = 'Marketing';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 2
FROM career c
JOIN learning_methods m ON m.title = 'Active Recall'
WHERE c.name = 'Marketing';

INSERT INTO major_learning_methods(career_id, learning_method_id, sort_order)
SELECT c.id, m.id, 3
FROM career c
JOIN learning_methods m ON m.title = 'Feedback Loop'
WHERE c.name = 'Marketing';
