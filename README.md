# CNG 334 Complete Group Coursework

Github Link: https://github.com/Khaled-Eldowa/OS_Coursework

Master branch: the complete coursework

No_Sync branch: the coursework without synchornization

By Khaled Eldowa and Eugene Owilla.

Program Description:

	A multithreaded Server that accepts the clients requests and assigns each request a thread 
	(SearchHandler) from a thread pool that has a certain capacity after which requests have to 
	wait in a queue (and started automatically once possible). If the queue is full, the requests are rejected (handled by the 
	RequestRejection Handler).

	Each SearchHandler thread has its own thread pool (in which only a certain number of threads
	can be running at a time). For each file, the SearchHandler creates a FileHandler thread for
	it from the thread pool. When each FileHandler is done, it notifies its SearchHandler of its
	results, and if there are other FileHandlers waiting, one of them is started automatically 
	by the thread pool. When all FileHandlers finish, the SearchHandler sends the results to the
	client and closes the their sockets.

	A FileHandler searches for the specified word in a file and increments a counter each time a 
	match occured. The search is CASE SENSITIVE and matches exact words (e.g. "dog" is not 
	matched in "dogma").

	We also implemented a client app for testing. It connects with the server then awaits 
	acknowledgment. If the request was not rejected, we ask the user for the word to search for, 
	then we send it to the server and wait for the results.

	Note: 
	MAX_RUNNING_REQUESTS, MAX_WAITING_REQUESTS, MAX_RUNNING_THREADS_PER_REQUEST are specified at 
	the beginning of the ServerApp code. Maybe later we can specify them as command line 
	parameters or take them from the user.

Synchronization:

	When the server start, it gathers all the files in the samples folder in a list. And for each 
	file, it matches it to a pair of locks (read and write) through a hash map.

	The files list and the asscoiated locks objects are passed to each SearchHandler thread. And 
	when a SearchHandler deploys a FileHandler thread, it passes to it a file and its associated 
	locks from the map. Then the FileHandler will try to acquire the read lock of the passed file 
	before operating on it, if its read lock was unavailable (not really applicable to this coursework 
	since all the threads only read), the thread will block till it acquires the lock. Then it 
	for the word, report the results, and release the lock.

	We also made sure that the reportFileResults() method in the SearchHandlers is synchronized to 
	avoid discrepancies if more than one FileHandler thread attempt to call it at the same time.

Testing the Program:

	You can test the server app with multiple client app instances, *but remember to open the server 
	before running the clients*. 
