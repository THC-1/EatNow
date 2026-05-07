// Run with:
// mongosh mongodb://localhost:27017/eatnow_llm database/09_llm_mongo_collections.js

db = db.getSiblingDB("eatnow_llm");

db.createCollection("llm_merchant_daily_reports", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["_id", "merchantId", "reportDate", "snapshot", "updatedAt"],
      properties: {
        _id: {
          bsonType: "string",
          description: "merchantId:reportDate, for example 8:2026-05-07"
        },
        merchantId: {
          bsonType: ["long", "int", "double", "decimal"],
          description: "Merchant id from MySQL merchant.id"
        },
        reportDate: {
          bsonType: "date",
          description: "The local business date for this daily analysis snapshot"
        },
        snapshot: {
          bsonType: "object",
          description: "Merchant daily analysis snapshot assembled from MySQL"
        },
        updatedAt: {
          bsonType: "date",
          description: "Mongo document update time"
        }
      }
    }
  },
  validationLevel: "moderate",
  validationAction: "warn"
});

db.llm_merchant_daily_reports.createIndex(
  { merchantId: 1, reportDate: -1 },
  { name: "idx_merchant_report_date" }
);

