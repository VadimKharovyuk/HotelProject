DELETE FROM city_mappings;

INSERT INTO city_mappings (input_name, iata_code, english_name) VALUES
-- Киев
('киев', 'IEV', 'Kiev'),
('київ', 'IEV', 'Kiev'),
('kiev', 'IEV', 'Kiev'),
('kyiv', 'IEV', 'Kiev'),
-- Москва
('москва', 'MOW', 'Moscow'),
('moscow', 'MOW', 'Moscow'),
-- Лондон
('лондон', 'LON', 'London'),
('london', 'LON', 'London'),
-- Париж
('париж', 'PAR', 'Paris'),
('paris', 'PAR', 'Paris'),
-- Нью-Йорк
('нью-йорк', 'NYC', 'New York'),
('нью йорк', 'NYC', 'New York'),
('new york', 'NYC', 'New York'),
('newyork', 'NYC', 'New York'),
-- Барселона
('барселона', 'BCN', 'Barcelona'),
('barcelona', 'BCN', 'Barcelona'),
-- Рим
('рим', 'ROM', 'Rome'),
('rome', 'ROM', 'Rome'),
('roma', 'ROM', 'Rome'),
-- Амстердам
('амстердам', 'AMS', 'Amsterdam'),
('amsterdam', 'AMS', 'Amsterdam'),
-- Берлин
('берлин', 'BER', 'Berlin'),
('berlin', 'BER', 'Berlin'),
-- Вена
('вена', 'VIE', 'Vienna'),
('vienna', 'VIE', 'Vienna'),
-- Прага
('прага', 'PRG', 'Prague'),
('prague', 'PRG', 'Prague');