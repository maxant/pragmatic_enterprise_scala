/*
 *   Copyright 2013 Ant Kutschera
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
select * from rating;

INSERT INTO RATING (  EVENT_UID , NUM_RATINGS , SUM_RATINGS ) 
VALUES ('BWEIY-A4', 1, 4)
;

delete from rating where event_uid = 'BWEIY-A4'
;

UPDATE RATING 
SET NUM_RATINGS = (SELECT NUM_RATINGS + 1 FROM RATING WHERE EVENT_UID = 'BWEIY-A4')
, SUM_RATINGS = (SELECT SUM_RATINGS + 4 FROM RATING WHERE EVENT_UID = 'BWEIY-A4')
WHERE EVENT_UID = 'BWEIY-A4' 
--AND EXISTS ( SELECT * FROM RATING WHERE EVENT_UID = 'BWEIY-A4')
;commit;

MERGE INTO RATING (EVENT_UID, NUM_RATINGS, SUM_RATINGS)
KEY(EVENT_UID)
VALUES(
    'BWEIY-A4',
    (SELECT NUM_RATINGS + 1 FROM RATING WHERE EVENT_UID = 'BWEIY-A4'),
    (SELECT SUM_RATINGS + 4 FROM RATING WHERE EVENT_UID = 'BWEIY-A4')
)
;

SELECT * FROM RATING WHERE EVENT_UID = 'BWEIY-A4';