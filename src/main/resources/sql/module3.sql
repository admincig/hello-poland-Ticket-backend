 /* 
 WAŻNE przed zrobieniem release'a na produkcje helloticket trzeba sprawdzić czy kod będzie poprawnie działał na aktualnej bazie.
 
 Należy wykonać poniższe zapytanie by sprawdzić czy w bazie nie znajdują się 2(lub więcej) tickety z numerem seryjnym,
 którego pierwsze 7 znaków się powtarza. Jako, że sprawdzenie dla konkretnie 7 znaków jest w kodzie, 
 należy to sprawdzić przed releasem. 
 Brak wyników -> wszystko dobrze, robimy release'a
 Jest wynik -> trzeba zmienić kod tak by były wyszukiwane numery seryjne dla większej ilości znaków. 
 
 */
 select  from tickets t where (select count(*) from tickets tk where left(tk.serial_number,7)=left(t.serial_number,7)) >1;