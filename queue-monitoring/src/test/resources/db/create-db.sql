CREATE TABLE contracts(
   id INTEGER PRIMARY KEY,
   region VARCHAR(50),
   country VARCHAR(50),
   status VARCHAR(30),
   eProject VARCHAR(30),
   sapContract VARCHAR(30),
   fl VARCHAR(30),
   soldToName VARCHAR(200),
   shipTo VARCHAR(200),
   customerNameEndUser VARCHAR(200),
   commentsAppsSuppTeam VARCHAR(400),
   sapOrder VARCHAR(30),
   startLastRenewed DATE,
   endContract DATE,
   solutionApplication VARCHAR(300),
   apsSuppMc VARCHAR(200),
   apsSuppDescription VARCHAR(300),
   linkToSapContractDoc VARCHAR(300),
   manualDate VARCHAR(50)
);

CREATE TABLE notification(
	srNumber VARCHAR(50) PRIMARY KEY,
	notificationDate TIMESTAMP(2),
	reminder INTEGER
);